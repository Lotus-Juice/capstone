package com.bookworm.core.common.exception;

import org.springframework.http.HttpStatus;

public class OrderProcessingException extends BaseException {

    public OrderProcessingException(String message) {
        super("ORDER_PROCESSING_FAILED", message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public OrderProcessingException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
