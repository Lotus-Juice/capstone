package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.CouponDTOs;
import com.bookworm.core.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Promo code discounts and coupon validation engine.")
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "Validate promo coupon against cart value", operationId = "validateCoupon")
    @PostMapping("/validate")
    public ResponseEntity<CouponDTOs.CouponValidationResponse> validate(
        @Valid @RequestBody CouponDTOs.ValidateCouponRequest request
    ) {
        return ResponseEntity.ok(couponService.validateCoupon(request));
    }

    @Operation(summary = "List active promotional coupons", operationId = "listAvailableCoupons")
    @GetMapping("/available")
    public ResponseEntity<List<CouponDTOs.CouponOfferDTO>> listOffers() {
        return ResponseEntity.ok(couponService.listAvailableOffers());
    }
}
