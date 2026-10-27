package com.bookworm.core.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseException {

    public DuplicateResourceException(String resourceName, String field, Object value) {
        super(
            "DUPLICATE_RESOURCE",
            String.format("%s with %s '%s' already exists", resourceName, field, value),
            HttpStatus.CONFLICT
        );
    }

    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message, HttpStatus.CONFLICT);
    }
}
