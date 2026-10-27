package com.bookworm.core.service;

import com.bookworm.api.v1.dto.CouponDTOs.CouponOfferDTO;
import com.bookworm.api.v1.dto.CouponDTOs.CouponValidationResponse;
import com.bookworm.api.v1.dto.CouponDTOs.ValidateCouponRequest;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.mapper.CouponMapper;
import com.bookworm.infra.persistence.entity.CouponEntity;
import com.bookworm.infra.persistence.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService Unit Tests")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponService couponService;

    private CouponEntity percentageCoupon;
    private CouponEntity flatCoupon;

    @BeforeEach
    void setUp() {
        percentageCoupon = CouponEntity.builder()
            .id(UUID.randomUUID())
            .code("WELCOME20")
            .discountType("PERCENTAGE")
            .discountValue(new BigDecimal("20.00"))
            .minOrderAmount(new BigDecimal("500.00"))
            .maxDiscountAmount(new BigDecimal("200.00"))
            .validFrom(Instant.now().minus(1, ChronoUnit.DAYS))
            .validUntil(Instant.now().plus(30, ChronoUnit.DAYS))
            .isActive(true)
            .build();

        flatCoupon = CouponEntity.builder()
            .id(UUID.randomUUID())
            .code("FLAT100")
            .discountType("FLAT_AMOUNT")
            .discountValue(new BigDecimal("100.00"))
            .minOrderAmount(new BigDecimal("400.00"))
            .validFrom(Instant.now().minus(1, ChronoUnit.DAYS))
            .validUntil(Instant.now().plus(30, ChronoUnit.DAYS))
            .isActive(true)
            .build();
    }

    @Nested
    @DisplayName("Validate Percentage Coupon")
    class PercentageCouponTests {

        @Test
        @DisplayName("Should apply 20% discount up to max threshold")
        void shouldApplyPercentageDiscount() {
            ValidateCouponRequest request = new ValidateCouponRequest();
            request.setCode("WELCOME20");
            request.setCartAmount(new BigDecimal("800.00")); // 20% of 800 = 160 < max 200

            given(couponRepository.findActiveCouponByCode(eq("WELCOME20"), any(Instant.class)))
                .willReturn(Optional.of(percentageCoupon));

            CouponValidationResponse response = couponService.validateCoupon(request);

            assertThat(response).isNotNull();
            assertThat(response.isValid()).isTrue();
            assertThat(response.getCalculatedDiscount()).isEqualByComparingTo(new BigDecimal("160.00"));
            assertThat(response.getNetPayable()).isEqualByComparingTo(new BigDecimal("640.00"));
        }

        @Test
        @DisplayName("Should cap percentage discount to max discount limit")
        void shouldCapDiscountToMaxThreshold() {
            ValidateCouponRequest request = new ValidateCouponRequest();
            request.setCode("WELCOME20");
            request.setCartAmount(new BigDecimal("2000.00")); // 20% of 2000 = 400 > max 200

            given(couponRepository.findActiveCouponByCode(eq("WELCOME20"), any(Instant.class)))
                .willReturn(Optional.of(percentageCoupon));

            CouponValidationResponse response = couponService.validateCoupon(request);

            assertThat(response).isNotNull();
            assertThat(response.getCalculatedDiscount()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(response.getNetPayable()).isEqualByComparingTo(new BigDecimal("1800.00"));
        }
    }

    @Nested
    @DisplayName("Validate Flat Coupon & Boundary Conditions")
    class FlatCouponAndValidationTests {

        @Test
        @DisplayName("Should apply flat discount correctly")
        void shouldApplyFlatDiscount() {
            ValidateCouponRequest request = new ValidateCouponRequest();
            request.setCode("FLAT100");
            request.setCartAmount(new BigDecimal("600.00"));

            given(couponRepository.findActiveCouponByCode(eq("FLAT100"), any(Instant.class)))
                .willReturn(Optional.of(flatCoupon));

            CouponValidationResponse response = couponService.validateCoupon(request);

            assertThat(response.getCalculatedDiscount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(response.getNetPayable()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should throw BusinessException when cart amount is below minimum spend requirement")
        void shouldThrowExceptionWhenMinimumSpendNotMet() {
            ValidateCouponRequest request = new ValidateCouponRequest();
            request.setCode("WELCOME20");
            request.setCartAmount(new BigDecimal("300.00")); // Min is 500

            given(couponRepository.findActiveCouponByCode(eq("WELCOME20"), any(Instant.class)))
                .willReturn(Optional.of(percentageCoupon));

            assertThatThrownBy(() -> couponService.validateCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Minimum order amount of ₹500.00 required");
        }

        @Test
        @DisplayName("Should throw BusinessException for non-existent or expired coupon")
        void shouldThrowExceptionForInvalidCoupon() {
            ValidateCouponRequest request = new ValidateCouponRequest();
            request.setCode("EXPIRED99");
            request.setCartAmount(new BigDecimal("1000.00"));

            given(couponRepository.findActiveCouponByCode(eq("EXPIRED99"), any(Instant.class)))
                .willReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.validateCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Coupon code is invalid or has expired");
        }
    }
}
