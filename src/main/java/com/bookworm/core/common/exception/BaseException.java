package com.bookworm.core.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base abstract checked/unchecked exception for all application-level errors.
 */
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    protected BaseException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    protected BaseException(String errorCode, String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
