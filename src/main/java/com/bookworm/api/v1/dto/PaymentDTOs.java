package com.bookworm.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

public final class PaymentDTOs {

    private PaymentDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InitiatePaymentRequest {

        @NotNull(message = "Order ID is required")
        private UUID orderId;

        @NotBlank(message = "Payment method is required")
        private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, WALLET

        @NotBlank(message = "Payment gateway is required")
        private String paymentGateway; // RAZORPAY, STRIPE, PAYTM

        @NotBlank(message = "Payment token is required for secure processing")
        private String paymentToken; // Tokenized card/gateway reference (PCI-DSS compliant)

        @Pattern(regexp = "^[\\w.-]+@[\\w.-]+$", message = "Invalid UPI VPA format")
        private String upiVpa;

        private String walletProvider;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InitiatePaymentResponse {
        private UUID paymentId;
        private UUID orderId;
        private BigDecimal payableAmount;
        private String currency;
        private String status;
        private String gatewayTransactionId;
        private String redirectUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerifyPaymentRequest {

        @NotNull(message = "Payment ID is required")
        private UUID paymentId;

        @NotBlank(message = "Gateway transaction ID is required")
        private String gatewayTransactionId;

        @NotBlank(message = "Gateway cryptographic signature is required")
        private String gatewaySignature;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerifyPaymentResponse {
        private UUID paymentId;
        private UUID orderId;
        private String status;
        private BigDecimal amount;
        private String orderStatus;
        private String message;
    }
}
