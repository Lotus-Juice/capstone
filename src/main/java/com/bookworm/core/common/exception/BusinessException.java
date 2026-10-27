package com.bookworm.core.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Standard business rule violation exception.
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super("BUSINESS_RULE_VIOLATION", message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String errorCode, String message, HttpStatus status) {
        super(errorCode, message, status);
    }
}
