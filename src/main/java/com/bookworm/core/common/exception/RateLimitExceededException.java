package com.bookworm.core.common.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends BaseException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds) {
        super("RATE_LIMIT_EXCEEDED", "Too many requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
