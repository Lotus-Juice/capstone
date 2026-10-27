package com.bookworm.core.service;

import com.bookworm.api.v1.dto.ShippingDTOs.ShipmentTrackingResponse;
import com.bookworm.api.v1.dto.ShippingDTOs.ShippingEstimateResponse;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.ShippingMapper;
import com.bookworm.infra.persistence.entity.ShipmentEntity;
import com.bookworm.infra.persistence.repository.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingService Unit Tests")
class ShippingServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShippingMapper shippingMapper;

    @InjectMocks
    private ShippingService shippingService;

    @Nested
    @DisplayName("Estimate Shipping SLA")
    class EstimateShippingTests {

        @Test
        @DisplayName("Should return delivery estimate for valid 6-digit Indian PIN code")
        void shouldReturnEstimateForValidPincode() {
            ShippingEstimateResponse response = shippingService.estimateShipping("560001");

            assertThat(response).isNotNull();
            assertThat(response.isServiceable()).isTrue();
            assertThat(response.getPincode()).isEqualTo("560001");
            assertThat(response.getCourierPartner()).isEqualTo("BlueDart Express");
        }

        @Test
        @DisplayName("Should throw BusinessException for invalid pincode format")
        void shouldThrowExceptionForInvalidPincode() {
            assertThatThrownBy(() -> shippingService.estimateShipping("INVALID_PIN"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid 6-digit Indian PIN code format");
        }
    }

    @Nested
    @DisplayName("Track Shipment")
    class TrackShipmentTests {

        @Test
        @DisplayName("Should return tracking details for valid waybill tracking number")
        void shouldTrackShipmentSuccessfully() {
            ShipmentEntity shipment = ShipmentEntity.builder()
                .id(UUID.randomUUID())
                .trackingNumber("BLUEDART-889977")
                .carrierName("BlueDart")
                .status("IN_TRANSIT")
                .build();

            ShipmentTrackingResponse trackingResponse = ShipmentTrackingResponse.builder()
                .trackingNumber("BLUEDART-889977")
                .carrierName("BlueDart")
                .status("IN_TRANSIT")
                .build();

            given(shipmentRepository.findByTrackingNumberWithCheckpoints("BLUEDART-889977"))
                .willReturn(Optional.of(shipment));
            given(shippingMapper.toTrackingResponse(shipment)).willReturn(trackingResponse);

            ShipmentTrackingResponse response = shippingService.trackShipment("BLUEDART-889977");

            assertThat(response).isNotNull();
            assertThat(response.getTrackingNumber()).isEqualTo("BLUEDART-889977");
            assertThat(response.getStatus()).isEqualTo("IN_TRANSIT");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for unknown tracking number")
        void shouldThrowExceptionWhenTrackingNotFound() {
            given(shipmentRepository.findByTrackingNumberWithCheckpoints("UNKNOWN_TRACK"))
                .willReturn(Optional.empty());

            assertThatThrownBy(() -> shippingService.trackShipment("UNKNOWN_TRACK"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Shipment with identifier UNKNOWN_TRACK not found");
        }
    }
}
