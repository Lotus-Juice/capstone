package com.bookworm.api.v1.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ShippingDTOs {

    private ShippingDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingEstimateResponse {
        private String pincode;
        private boolean isServiceable;
        private LocalDate estimatedDeliveryDate;
        private String displayLabel;
        private BigDecimal shippingFee;
        private String courierPartner;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrackingCheckpointDTO {
        private String checkpointStatus;
        private String location;
        private String description;
        private Instant scannedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShipmentTrackingResponse {
        private String trackingNumber;
        private String carrierName;
        private String status;
        private LocalDate estimatedDeliveryDate;
        private List<TrackingCheckpointDTO> checkpoints;
    }
}
