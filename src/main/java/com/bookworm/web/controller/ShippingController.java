package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.ShippingDTOs;
import com.bookworm.core.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping", description = "Pincode serviceability, delivery estimation, and live tracking checkpoints.")
public class ShippingController {

    private final ShippingService shippingService;

    @Operation(summary = "Check pincode serviceability and estimated delivery date (EDD)", operationId = "getShippingEstimate")
    @GetMapping("/estimate")
    public ResponseEntity<ShippingDTOs.ShippingEstimateResponse> getEstimate(
        @RequestParam("pincode") @Pattern(regexp = "^\\d{6}$", message = "Pincode must be exactly 6 digits") String pincode
    ) {
        return ResponseEntity.ok(shippingService.estimateShipping(pincode));
    }

    @Operation(summary = "Track shipment parcel milestones", operationId = "trackShipment")
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShippingDTOs.ShipmentTrackingResponse> trackShipment(
        @PathVariable("trackingNumber") String trackingNumber
    ) {
        return ResponseEntity.ok(shippingService.trackShipment(trackingNumber));
    }
}
