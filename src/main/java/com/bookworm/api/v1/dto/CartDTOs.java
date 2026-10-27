package com.bookworm.api.v1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CartDTOs {

    private CartDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddCartItemRequest {

        @NotNull(message = "Variant ID is required")
        private UUID variantId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 20, message = "Quantity cannot exceed 20 per item")
        @Builder.Default
        private Integer quantity = 1;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateCartItemRequest {

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 20, message = "Quantity cannot exceed 20")
        private Integer quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MergeCartRequest {

        @NotBlank(message = "Guest session token is required")
        private String guestSessionToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemDTO {
        private UUID cartItemId;
        private UUID variantId;
        private UUID bookId;
        private String title;
        private String author;
        private String format;
        private String coverImageUrl;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal lineTotal;
        private boolean inStock;
        private String deliveryEstimate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartSummaryDTO {
        private BigDecimal itemsTotal;
        private BigDecimal taxAmount;
        private BigDecimal deliveryCharges;
        private BigDecimal discountAmount;
        private String couponCode;
        private BigDecimal grandTotal;
        private String currency;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartDTO {
        private UUID cartId;
        private String guestSessionToken;
        @Builder.Default
        private List<CartItemDTO> items = new ArrayList<>();
        private int itemsCount;
        private CartSummaryDTO summary;
    }
}
