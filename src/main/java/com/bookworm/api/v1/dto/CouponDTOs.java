package com.bookworm.api.v1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class CouponDTOs {

    private CouponDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidateCouponRequest {

        @NotBlank(message = "Coupon code is required")
        private String code;

        @NotNull(message = "Cart amount is required")
        @DecimalMin(value = "0.01", message = "Cart amount must be greater than zero")
        private BigDecimal cartAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CouponValidationResponse {
        private String couponCode;
        private String discountType; // FLAT_AMOUNT, PERCENTAGE
        private BigDecimal discountValue;
        private BigDecimal calculatedDiscount;
        private BigDecimal netPayable;
        private boolean isValid;
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CouponOfferDTO {
        private String code;
        private String description;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal minOrderAmount;
        private Instant validUntil;
    }
}
