package com.bookworm.core.service;

import com.bookworm.api.v1.dto.CartDTOs;
import com.bookworm.api.v1.dto.OrderDTOs;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.common.exception.InsufficientStockException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.OrderMapper;
import com.bookworm.infra.persistence.entity.*;
import com.bookworm.infra.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserAddressRepository userAddressRepository;
    private final BookVariantRepository bookVariantRepository;
    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final BookRepository bookRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDTOs.OrderDetailResponse checkoutOrder(UUID userId, OrderDTOs.CreateOrderRequest request) {
        log.info("Initiating checkout for user ID: {}, cart ID: {}", userId, request.getCartId());

        CartEntity cart = cartRepository.findById(request.getCartId())
            .orElseThrow(() -> new ResourceNotFoundException("Cart", request.getCartId()));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException("EMPTY_CART", "Cannot checkout an empty shopping cart.");
        }

        UserAddressEntity address = userAddressRepository.findByIdAndUserId(request.getShippingAddressId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("ShippingAddress", request.getShippingAddressId()));

        // Calculate Pricing Snapshot
        BigDecimal itemsSubtotal = BigDecimal.ZERO;
        List<OrderLineItemEntity> lineItems = new ArrayList<>();

        for (CartItemEntity item : cart.getItems()) {
            BookVariantEntity variant = item.getVariant();
            int updatedRows = bookVariantRepository.deductStockIfAvailable(variant.getId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new InsufficientStockException(variant.getSku(), item.getQuantity(), variant.getStockQuantity());
            }

            BigDecimal lineTotal = variant.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemsSubtotal = itemsSubtotal.add(lineTotal);

            String authorName = (variant.getBook().getBookAuthors() != null && !variant.getBook().getBookAuthors().isEmpty())
                ? variant.getBook().getBookAuthors().get(0).getAuthor().getName() : "Unknown Author";

            OrderLineItemEntity lineItem = OrderLineItemEntity.builder()
                .variant(variant)
                .bookTitleSnapshot(variant.getBook().getTitle())
                .authorNameSnapshot(authorName)
                .formatTypeSnapshot(variant.getFormatType())
                .unitPrice(variant.getSellingPrice())
                .quantity(item.getQuantity())
                .lineTotal(lineTotal)
                .build();

            lineItems.add(lineItem);
        }

        // Apply GST and Shipping Rules
        BigDecimal tax = itemsSubtotal.multiply(new BigDecimal("0.12")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal shippingFee = itemsSubtotal.compareTo(new BigDecimal("500.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("40.00");
        BigDecimal discount = BigDecimal.ZERO;
        CouponEntity appliedCoupon = null;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            appliedCoupon = couponRepository.findActiveCouponByCode(request.getCouponCode(), Instant.now())
                .orElseThrow(() -> new BusinessException("INVALID_COUPON", "Coupon code is invalid or expired."));

            if (itemsSubtotal.compareTo(appliedCoupon.getMinOrderAmount()) < 0) {
                throw new BusinessException("COUPON_MIN_AMOUNT_NOT_MET", "Minimum order amount not met for coupon " + appliedCoupon.getCode());
            }

            if ("PERCENTAGE".equalsIgnoreCase(appliedCoupon.getDiscountType())) {
                discount = itemsSubtotal.multiply(appliedCoupon.getDiscountValue().divide(new BigDecimal("100.00")));
                if (appliedCoupon.getMaxDiscountAmount() != null && discount.compareTo(appliedCoupon.getMaxDiscountAmount()) > 0) {
                    discount = appliedCoupon.getMaxDiscountAmount();
                }
            } else {
                discount = appliedCoupon.getDiscountValue();
            }
        }

        BigDecimal grandTotal = itemsSubtotal.add(tax).add(shippingFee).subtract(discount);

        String orderNumber = "BW-" + LocalDate.now().getYear() + "-" + (10000 + (int)(Math.random() * 89999));

        OrderEntity order = OrderEntity.builder()
            .orderNumber(orderNumber)
            .user(cart.getUser())
            .shippingAddress(address)
            .coupon(appliedCoupon)
            .itemsSubtotal(itemsSubtotal)
            .taxAmount(tax)
            .shippingFee(shippingFee)
            .discountAmount(discount)
            .grandTotal(grandTotal)
            .currency("INR")
            .status("PENDING_PAYMENT")
            .estimatedDeliveryDate(LocalDate.now().plusDays(4))
            .placedAt(Instant.now())
            .lineItems(new ArrayList<>())
            .build();

        for (OrderLineItemEntity li : lineItems) {
            li.setOrder(order);
            order.getLineItems().add(li);
        }

        OrderEntity savedOrder = orderRepository.save(order);

        if (appliedCoupon != null) {
            CouponRedemptionEntity redemption = CouponRedemptionEntity.builder()
                .coupon(appliedCoupon)
                .user(cart.getUser())
                .order(savedOrder)
                .discountAmount(discount)
                .build();
            couponRedemptionRepository.save(redemption);
        }

        // Clear checked-out cart items
        cart.getItems().clear();
        cart.setStatus("CHECKED_OUT");
        cartRepository.save(cart);

        log.info("Order created successfully. Order Number: {}, Grand Total: {}", savedOrder.getOrderNumber(), savedOrder.getGrandTotal());
        return orderMapper.toDetailResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDTOs.OrderDetailResponse getOrderDetails(UUID userId, UUID orderId) {
        OrderEntity order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        return orderMapper.toDetailResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTOs.OrderHistorySummaryDTO> listOrders(UUID userId, Pageable pageable) {
        log.info("Fetching order history for user ID: {}", userId);
        Page<OrderEntity> orders = orderRepository.findByUserIdOrderByPlacedAtDesc(userId, pageable);
        return orders.map(orderMapper::toHistorySummaryDTO);
    }

    @Transactional
    public CartDTOs.CartDTO reorder(UUID userId, UUID orderId) {
        log.info("Executing 1-click reorder ('Buy It Again') for user ID: {}, order ID: {}", userId, orderId);

        OrderEntity pastOrder = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        for (OrderLineItemEntity item : pastOrder.getLineItems()) {
            CartDTOs.AddCartItemRequest addReq = CartDTOs.AddCartItemRequest.builder()
                .variantId(item.getVariant().getId())
                .quantity(item.getQuantity())
                .build();
            cartService.addItemToCart(userId, null, addReq);
        }

        return cartService.getActiveCart(userId, null);
    }

    @Transactional
    public OrderDTOs.CancelOrderResponse cancelOrder(UUID userId, UUID orderId, OrderDTOs.CancelOrderRequest request) {
        log.info("Cancelling order ID: {} for user ID: {}, Reason: {}", orderId, userId, request.getReason());

        OrderEntity order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException("ORDER_NOT_CANCELLABLE", "Orders in status '" + order.getStatus() + "' cannot be cancelled.");
        }

        // Restore deducted inventory
        for (OrderLineItemEntity item : order.getLineItems()) {
            bookVariantRepository.restoreStock(item.getVariant().getId(), item.getQuantity());
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        return OrderDTOs.CancelOrderResponse.builder()
            .orderId(order.getId())
            .status("CANCELLED")
            .refundInitiated(true)
            .refundAmount(order.getGrandTotal())
            .build();
    }
}
