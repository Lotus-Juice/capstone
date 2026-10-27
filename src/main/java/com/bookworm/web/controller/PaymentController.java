package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.PaymentDTOs;
import com.bookworm.core.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment gateways, tokenization, verification, and webhooks.")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Initiate payment gateway charge intent", operationId = "initiatePayment")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/initiate")
    public ResponseEntity<PaymentDTOs.InitiatePaymentResponse> initiatePayment(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody PaymentDTOs.InitiatePaymentRequest request
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(paymentService.initiatePayment(effectiveUserId, request));
    }

    @Operation(summary = "Verify payment signature and confirm transaction", operationId = "verifyPayment")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/verify")
    public ResponseEntity<PaymentDTOs.VerifyPaymentResponse> verifyPayment(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @Valid @RequestBody PaymentDTOs.VerifyPaymentRequest request
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(paymentService.verifyPayment(effectiveUserId, request));
    }

    @Operation(summary = "Gateway webhook for asynchronous payment status notification", operationId = "handlePaymentWebhook")
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> webhook(
        @RequestBody Map<String, Object> payload,
        @RequestHeader(value = "X-Signature", required = false) String signature
    ) {
        log.info("Received payment gateway webhook notification");
        return ResponseEntity.ok(Map.of("status", "acknowledged"));
    }
}
