package com.bookworm.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class OrderDTOs {

    private OrderDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateOrderRequest {

        @NotNull(message = "Cart ID is required")
        private UUID cartId;

        @NotNull(message = "Shipping address ID is required")
        private UUID shippingAddressId;

        private String couponCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancelOrderRequest {

        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderLineItemDTO {
        private String bookTitle;
        private String author;
        private String format;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal lineTotal;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDetailResponse {
        private UUID orderId;
        private String orderNumber;
        private String status;
        private BigDecimal itemsSubtotal;
        private BigDecimal taxAmount;
        private BigDecimal shippingFee;
        private BigDecimal discountAmount;
        private BigDecimal grandTotal;
        private String currency;
        private Instant placedAt;
        private LocalDate estimatedDeliveryDate;
        private AddressDTOs.AddressDTO shippingAddress;
        private List<OrderLineItemDTO> lineItems;
        private PaymentSummaryDTO payment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentSummaryDTO {
        private String paymentMethod;
        private String maskedCard;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderHistorySummaryDTO {
        private UUID orderId;
        private String orderNumber;
        private Instant placedAt;
        private String status;
        private BigDecimal grandTotal;
        private List<ItemPreviewDTO> itemsPreview;
        private boolean canReorder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemPreviewDTO {
        private String title;
        private String coverImageUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancelOrderResponse {
        private UUID orderId;
        private String status;
        private boolean refundInitiated;
        private BigDecimal refundAmount;
    }
}
