package com.bookworm.core.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message, HttpStatus.UNAUTHORIZED);
    }
}
