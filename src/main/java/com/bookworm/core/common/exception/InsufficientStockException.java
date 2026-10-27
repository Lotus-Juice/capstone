package com.bookworm.core.common.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException {

    public InsufficientStockException(String sku, int requested, int available) {
        super(
            "INSUFFICIENT_STOCK",
            String.format("Insufficient stock for SKU '%s'. Requested: %d, Available: %d", sku, requested, available),
            HttpStatus.CONFLICT
        );
    }

    public InsufficientStockException(String message) {
        super("INSUFFICIENT_STOCK", message, HttpStatus.CONFLICT);
    }
}
