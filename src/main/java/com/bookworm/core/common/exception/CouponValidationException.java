package com.bookworm.core.common.exception;

import org.springframework.http.HttpStatus;

public class CouponValidationException extends BaseException {

    public CouponValidationException(String message) {
        super("COUPON_INVALID", message, HttpStatus.BAD_REQUEST);
    }

    public CouponValidationException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }
}
