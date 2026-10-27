package com.bookworm.core.service;

import com.bookworm.api.v1.dto.PaymentDTOs.InitiatePaymentRequest;
import com.bookworm.api.v1.dto.PaymentDTOs.InitiatePaymentResponse;
import com.bookworm.api.v1.dto.PaymentDTOs.VerifyPaymentRequest;
import com.bookworm.api.v1.dto.PaymentDTOs.VerifyPaymentResponse;
import com.bookworm.core.common.exception.PaymentProcessingException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.PaymentMapper;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.entity.BookVariantEntity;
import com.bookworm.infra.persistence.entity.OrderEntity;
import com.bookworm.infra.persistence.entity.OrderLineItemEntity;
import com.bookworm.infra.persistence.entity.PaymentTransactionEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import com.bookworm.infra.persistence.repository.OrderRepository;
import com.bookworm.infra.persistence.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private OrderEntity sampleOrder;
    private PaymentTransactionEntity sampleTransaction;
    private BookEntity sampleBook;
    private BookVariantEntity sampleVariant;

    @BeforeEach
    void setUp() {
        sampleBook = BookEntity.builder()
            .id(UUID.randomUUID())
            .title("Clean Architecture")
            .copiesSold(100)
            .build();

        sampleVariant = BookVariantEntity.builder()
            .id(UUID.randomUUID())
            .book(sampleBook)
            .sellingPrice(new BigDecimal("600.00"))
            .build();

        sampleOrder = OrderEntity.builder()
            .id(UUID.randomUUID())
            .orderNumber("BW-2025-99999")
            .grandTotal(new BigDecimal("672.00"))
            .currency("INR")
            .status("PENDING_PAYMENT")
            .lineItems(new ArrayList<>())
            .build();

        OrderLineItemEntity lineItem = OrderLineItemEntity.builder()
            .id(UUID.randomUUID())
            .order(sampleOrder)
            .variant(sampleVariant)
            .quantity(1)
            .build();
        sampleOrder.getLineItems().add(lineItem);

        sampleTransaction = PaymentTransactionEntity.builder()
            .id(UUID.randomUUID())
            .order(sampleOrder)
            .gatewayTransactionId("pay_123456789")
            .paymentMethod("UPI")
            .paymentGateway("RAZORPAY")
            .amount(new BigDecimal("672.00"))
            .currency("INR")
            .status("INITIATED")
            .build();
    }

    @Nested
    @DisplayName("Initiate Payment")
    class InitiatePaymentTests {

        @Test
        @DisplayName("Should successfully initiate payment intent for pending order")
        void shouldInitiatePaymentSuccessfully() {
            InitiatePaymentRequest request = new InitiatePaymentRequest();
            request.setOrderId(sampleOrder.getId());
            request.setPaymentMethod("UPI");
            request.setPaymentGateway("RAZORPAY");
            request.setUpiVpa("customer@okhdfcbank");

            given(orderRepository.findById(sampleOrder.getId())).willReturn(Optional.of(sampleOrder));
            given(paymentTransactionRepository.save(any(PaymentTransactionEntity.class))).willReturn(sampleTransaction);

            InitiatePaymentResponse response = paymentService.initiatePayment(UUID.randomUUID(), request);

            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(sampleOrder.getId());
            assertThat(response.getPayableAmount()).isEqualByComparingTo(new BigDecimal("672.00"));
            assertThat(response.getStatus()).isEqualTo("PENDING_AUTH");
            verify(paymentTransactionRepository).save(any(PaymentTransactionEntity.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for unknown order ID")
        void shouldThrowExceptionWhenOrderNotFound() {
            UUID unknownOrderId = UUID.randomUUID();
            InitiatePaymentRequest request = new InitiatePaymentRequest();
            request.setOrderId(unknownOrderId);
            request.setPaymentMethod("UPI");
            request.setPaymentGateway("RAZORPAY");

            given(orderRepository.findById(unknownOrderId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.initiatePayment(UUID.randomUUID(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
        }
    }

    @Nested
    @DisplayName("Verify Payment")
    class VerifyPaymentTests {

        @Test
        @DisplayName("Should verify payment, update order status to CONFIRMED, and increment book sales velocity")
        void shouldVerifyPaymentSuccessfully() {
            VerifyPaymentRequest request = new VerifyPaymentRequest();
            request.setPaymentId(sampleTransaction.getId());
            request.setGatewaySignature("valid_signature_hash_xyz");

            given(paymentTransactionRepository.findById(sampleTransaction.getId())).willReturn(Optional.of(sampleTransaction));

            VerifyPaymentResponse response = paymentService.verifyPayment(UUID.randomUUID(), request);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("CAPTURED");
            assertThat(response.getOrderStatus()).isEqualTo("CONFIRMED");
            assertThat(sampleTransaction.getStatus()).isEqualTo("CAPTURED");
            assertThat(sampleOrder.getStatus()).isEqualTo("CONFIRMED");

            verify(paymentTransactionRepository).save(sampleTransaction);
            verify(orderRepository).save(sampleOrder);
            verify(bookRepository).incrementCopiesSold(sampleBook.getId(), 1);
        }

        @Test
        @DisplayName("Should mark transaction FAILED and throw PaymentProcessingException when gateway signature is blank")
        void shouldFailPaymentWhenSignatureInvalid() {
            VerifyPaymentRequest request = new VerifyPaymentRequest();
            request.setPaymentId(sampleTransaction.getId());
            request.setGatewaySignature(""); // Empty signature

            given(paymentTransactionRepository.findById(sampleTransaction.getId())).willReturn(Optional.of(sampleTransaction));

            assertThatThrownBy(() -> paymentService.verifyPayment(UUID.randomUUID(), request))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("Invalid gateway signature");

            assertThat(sampleTransaction.getStatus()).isEqualTo("FAILED");
            verify(paymentTransactionRepository).save(sampleTransaction);
        }
    }
}
