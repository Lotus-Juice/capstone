package com.bookworm.web.advice;

import com.bookworm.core.common.exception.BaseException;
import com.bookworm.core.common.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enterprise-grade global exception handler conforming strictly to IETF RFC 9457 (Problem Details).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_PROBLEM_URI = "https://api.bookworm.example.com/errors/";

    /**
     * 1. Handles all custom domain BaseExceptions (BusinessException, ResourceNotFoundException,
     * InsufficientStockException, DuplicateResourceException, PaymentProcessingException, etc.).
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.warn("Business domain exception [{}]: {} on URI: {}", ex.getErrorCode(), ex.getMessage(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle(formatTitleFromErrorCode(ex.getErrorCode()));
        problem.setType(URI.create(BASE_PROBLEM_URI + ex.getErrorCode().toLowerCase().replace('_', '-')));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", ex.getErrorCode());

        if (ex instanceof RateLimitExceededException rateLimitEx) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Retry-After", String.valueOf(rateLimitEx.getRetryAfterSeconds()));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).headers(headers).body(problem);
        }

        return ResponseEntity.status(ex.getStatus()).body(problem);
    }

    /**
     * 2. Handles Request Body (@Valid) validation failures.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        log.warn("Payload validation failed on URI: {}", request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request body failed validation constraints. Please check 'invalidParams' for details."
        );
        problem.setTitle("Validation Failed");
        problem.setType(URI.create(BASE_PROBLEM_URI + "validation-failed"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "VALIDATION_FAILED");

        List<Map<String, String>> invalidParams = ex.getBindingResult().getFieldErrors().stream()
            .map(this::mapFieldError)
            .toList();

        problem.setProperty("invalidParams", invalidParams);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * 3. Handles Request Parameter / Path Variable (@Validated) validation failures.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        log.warn("Constraint violation on URI: {}", request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Query or path parameter failed validation constraints."
        );
        problem.setTitle("Constraint Violation");
        problem.setType(URI.create(BASE_PROBLEM_URI + "constraint-violation"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "CONSTRAINT_VIOLATION");

        List<Map<String, String>> invalidParams = ex.getConstraintViolations().stream()
            .map(this::mapConstraintViolation)
            .toList();

        problem.setProperty("invalidParams", invalidParams);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * 4. Handles malformed JSON payloads, unknown fields, or syntax errors.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpServletRequest request
    ) {
        log.warn("Malformed HTTP request body on URI: {}", request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Malformed JSON request payload or invalid data type format."
        );
        problem.setTitle("Malformed Request Payload");
        problem.setType(URI.create(BASE_PROBLEM_URI + "malformed-payload"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "MALFORMED_JSON");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * 5. Handles missing required headers (e.g. Idempotency-Key or X-Guest-Session-Token).
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingHeader(
        MissingRequestHeaderException ex,
        HttpServletRequest request
    ) {
        log.warn("Missing required header: {} on URI: {}", ex.getHeaderName(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            String.format("Required request header '%s' is missing.", ex.getHeaderName())
        );
        problem.setTitle("Missing Request Header");
        problem.setType(URI.create(BASE_PROBLEM_URI + "missing-header"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "MISSING_HEADER");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * 6. Handles missing required query parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingQueryParam(
        MissingServletRequestParameterException ex,
        HttpServletRequest request
    ) {
        log.warn("Missing required parameter: {} on URI: {}", ex.getParameterName(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            String.format("Required query parameter '%s' of type '%s' is missing.", ex.getParameterName(), ex.getParameterType())
        );
        problem.setTitle("Missing Query Parameter");
        problem.setType(URI.create(BASE_PROBLEM_URI + "missing-parameter"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "MISSING_QUERY_PARAMETER");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * 7. Handles path parameter type mismatches (e.g., non-UUID string passed into UUID path).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        log.warn("Argument type mismatch for parameter: {} on URI: {}", ex.getName(), request.getRequestURI());

        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            String.format("Parameter '%s' value '%s' is not of expected type '%s'.", ex.getName(), ex.getValue(), requiredType)
        );
        problem.setTitle("Type Mismatch");
        problem.setType(URI.create(BASE_PROBLEM_URI + "type-mismatch"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "TYPE_MISMATCH");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * 8. Handles HTTP Method Not Allowed (405).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpServletRequest request
    ) {
        log.warn("HTTP method {} not supported on URI: {}", ex.getMethod(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()))
        );
        problem.setTitle("Method Not Allowed");
        problem.setType(URI.create(BASE_PROBLEM_URI + "method-not-allowed"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "METHOD_NOT_ALLOWED");

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problem);
    }

    /**
     * 9. Handles Spring Security Access Denied exceptions (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(
        AccessDeniedException ex,
        HttpServletRequest request
    ) {
        log.warn("Access denied on URI: {} - {}", request.getRequestURI(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "Access is denied. You do not possess the required role entitlements to access this resource."
        );
        problem.setTitle("Access Denied");
        problem.setType(URI.create(BASE_PROBLEM_URI + "forbidden"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "ACCESS_DENIED");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    /**
     * 10. Handles Spring Security Authentication failures (401 Unauthorized).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(
        AuthenticationException ex,
        HttpServletRequest request
    ) {
        log.warn("Authentication failure on URI: {} - {}", request.getRequestURI(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "Authentication failed. Please provide a valid Bearer token or credentials."
        );
        problem.setTitle("Authentication Failed");
        problem.setType(URI.create(BASE_PROBLEM_URI + "unauthorized"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "UNAUTHORIZED");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    /**
     * 11. Generic fallback for unexpected 500 Internal Server Errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnhandledException(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error("Unhandled internal server exception on URI: {}", request.getRequestURI(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected internal error occurred on the server. Please contact support with the timestamp."
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create(BASE_PROBLEM_URI + "internal-server-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "INTERNAL_SERVER_ERROR");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private Map<String, String> mapFieldError(FieldError fieldError) {
        Map<String, String> error = new HashMap<>();
        error.put("field", fieldError.getField());
        error.put("rejectedValue", String.valueOf(fieldError.getRejectedValue()));
        error.put("reason", fieldError.getDefaultMessage());
        return error;
    }

    private Map<String, String> mapConstraintViolation(ConstraintViolation<?> violation) {
        Map<String, String> error = new HashMap<>();
        error.put("property", violation.getPropertyPath().toString());
        error.put("invalidValue", String.valueOf(violation.getInvalidValue()));
        error.put("reason", violation.getMessage());
        return error;
    }

    private String formatTitleFromErrorCode(String errorCode) {
        String[] parts = errorCode.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(part.substring(0, 1).toUpperCase())
                  .append(part.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
