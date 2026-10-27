package com.bookworm.core.service;

import com.bookworm.api.v1.dto.CouponDTOs;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.mapper.CouponMapper;
import com.bookworm.infra.persistence.entity.CouponEntity;
import com.bookworm.infra.persistence.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Transactional(readOnly = true)
    public CouponDTOs.CouponValidationResponse validateCoupon(CouponDTOs.ValidateCouponRequest request) {
        log.info("Validating coupon code: '{}' for cart amount: {}", request.getCode(), request.getCartAmount());

        CouponEntity coupon = couponRepository.findActiveCouponByCode(request.getCode(), Instant.now())
            .orElseThrow(() -> new BusinessException("INVALID_COUPON", "Coupon code is invalid or has expired."));

        if (request.getCartAmount().compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BusinessException("MIN_AMOUNT_NOT_MET",
                String.format("Minimum order amount of ₹%s required for this coupon.", coupon.getMinOrderAmount()));
        }

        BigDecimal discount;
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = request.getCartAmount().multiply(coupon.getDiscountValue().divide(new BigDecimal("100.00")));
            if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        BigDecimal netPayable = request.getCartAmount().subtract(discount).max(BigDecimal.ZERO);

        return CouponDTOs.CouponValidationResponse.builder()
            .couponCode(coupon.getCode())
            .discountType(coupon.getDiscountType())
            .discountValue(coupon.getDiscountValue())
            .calculatedDiscount(discount)
            .netPayable(netPayable)
            .isValid(true)
            .message(String.format("Coupon applied successfully. You saved ₹%s!", discount))
            .build();
    }

    @Transactional(readOnly = true)
    public List<CouponDTOs.CouponOfferDTO> listAvailableOffers() {
        log.info("Fetching all active promotional coupon offers");
        List<CouponEntity> activeOffers = couponRepository.findAllActiveOffers(Instant.now());
        return couponMapper.toOfferDTOList(activeOffers);
    }
}
