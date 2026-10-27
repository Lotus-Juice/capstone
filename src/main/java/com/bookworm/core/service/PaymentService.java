package com.bookworm.core.service;

import com.bookworm.api.v1.dto.PaymentDTOs;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.common.exception.PaymentProcessingException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.PaymentMapper;
import com.bookworm.infra.persistence.entity.OrderEntity;
import com.bookworm.infra.persistence.entity.PaymentTransactionEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import com.bookworm.infra.persistence.repository.OrderRepository;
import com.bookworm.infra.persistence.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentDTOs.InitiatePaymentResponse initiatePayment(UUID userId, PaymentDTOs.InitiatePaymentRequest request) {
        log.info("Initiating payment for order ID: {}, method: {}, gateway: {}",
            request.getOrderId(), request.getPaymentMethod(), request.getPaymentGateway());

        OrderEntity order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("INVALID_ORDER_STATUS", "Order is not in payable state.");
        }

        String simulatedGatewayRef = "pay_" + UUID.randomUUID().toString().substring(0, 14);

        PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
            .order(order)
            .gatewayTransactionId(simulatedGatewayRef)
            .paymentMethod(request.getPaymentMethod())
            .paymentGateway(request.getPaymentGateway())
            .amount(order.getGrandTotal())
            .currency(order.getCurrency())
            .status("INITIATED")
            .upiVpa(request.getUpiVpa())
            .build();

        PaymentTransactionEntity savedTransaction = paymentTransactionRepository.save(transaction);
        log.info("Payment transaction initialized with ID: {}", savedTransaction.getId());

        return PaymentDTOs.InitiatePaymentResponse.builder()
            .paymentId(savedTransaction.getId())
            .orderId(order.getId())
            .payableAmount(order.getGrandTotal())
            .currency(order.getCurrency())
            .status("PENDING_AUTH")
            .gatewayTransactionId(simulatedGatewayRef)
            .build();
    }

    @Transactional
    public PaymentDTOs.VerifyPaymentResponse verifyPayment(UUID userId, PaymentDTOs.VerifyPaymentRequest request) {
        log.info("Verifying payment transaction ID: {}", request.getPaymentId());

        PaymentTransactionEntity transaction = paymentTransactionRepository.findById(request.getPaymentId())
            .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", request.getPaymentId()));

        if (request.getGatewaySignature() == null || request.getGatewaySignature().isBlank()) {
            transaction.setStatus("FAILED");
            paymentTransactionRepository.save(transaction);
            throw new PaymentProcessingException("SIGNATURE_FAILED", "Invalid gateway signature. Verification aborted.");
        }

        transaction.setStatus("CAPTURED");
        transaction.setPaidAt(Instant.now());
        paymentTransactionRepository.save(transaction);

        OrderEntity order = transaction.getOrder();
        order.setStatus("CONFIRMED");
        orderRepository.save(order);

        // Increment sales velocity on confirmed order
        order.getLineItems().forEach(li -> {
            if (li.getVariant() != null && li.getVariant().getBook() != null) {
                bookRepository.incrementCopiesSold(li.getVariant().getBook().getId(), li.getQuantity());
            }
        });

        log.info("Payment ID: {} confirmed. Order ID: {} status set to CONFIRMED", transaction.getId(), order.getId());

        return PaymentDTOs.VerifyPaymentResponse.builder()
            .paymentId(transaction.getId())
            .orderId(order.getId())
            .status("CAPTURED")
            .amount(transaction.getAmount())
            .orderStatus("CONFIRMED")
            .message("Your purchase of the following reads is successful.")
            .build();
    }
}
