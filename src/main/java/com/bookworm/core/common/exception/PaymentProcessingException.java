package com.bookworm.core.common.exception;

import org.springframework.http.HttpStatus;

public class PaymentProcessingException extends BaseException {

    public PaymentProcessingException(String message) {
        super("PAYMENT_FAILED", message, HttpStatus.BAD_REQUEST);
    }

    public PaymentProcessingException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }
}
