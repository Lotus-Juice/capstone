package com.bookworm.core.service;

import com.bookworm.api.v1.dto.ShippingDTOs;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.ShippingMapper;
import com.bookworm.infra.persistence.entity.ShipmentEntity;
import com.bookworm.infra.persistence.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final ShippingMapper shippingMapper;

    @Transactional(readOnly = true)
    public ShippingDTOs.ShippingEstimateResponse estimateShipping(String pincode) {
        log.info("Calculating delivery estimate for PIN code: {}", pincode);

        if (pincode == null || !pincode.matches("^\\d{6}$")) {
            throw new BusinessException("INVALID_PINCODE", "Invalid 6-digit Indian PIN code format.");
        }

        // Standard 4-day logistics SLA
        LocalDate estimatedDate = LocalDate.now().plusDays(4);
        String dayOfWeek = estimatedDate.getDayOfWeek().name().substring(0, 3);
        String displayLabel = String.format("Delivery by %s, %d %s",
            dayOfWeek.substring(0, 1) + dayOfWeek.substring(1).toLowerCase(),
            estimatedDate.getDayOfMonth(),
            estimatedDate.getMonth().name().substring(0, 3));

        return ShippingDTOs.ShippingEstimateResponse.builder()
            .pincode(pincode)
            .isServiceable(true)
            .estimatedDeliveryDate(estimatedDate)
            .displayLabel(displayLabel)
            .shippingFee(BigDecimal.ZERO)
            .courierPartner("BlueDart Express")
            .build();
    }

    @Transactional(readOnly = true)
    public ShippingDTOs.ShipmentTrackingResponse trackShipment(String trackingNumber) {
        log.info("Fetching tracking updates for waybill: {}", trackingNumber);

        ShipmentEntity shipment = shipmentRepository.findByTrackingNumberWithCheckpoints(trackingNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment", trackingNumber));

        return shippingMapper.toTrackingResponse(shipment);
    }
}
