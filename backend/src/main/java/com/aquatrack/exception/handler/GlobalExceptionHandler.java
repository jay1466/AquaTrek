package com.aquatrack.exception.handler;

import com.aquatrack.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the entire AquaTrack REST API.
 *
 * <p>Intercepts all exceptions thrown from controllers and service layer
 * and converts them to structured {@link ErrorResponse} objects with
 * appropriate HTTP status codes.</p>
 *
 * <p>The handler covers:
 * <ul>
 *   <li>All custom {@link AquaTrackException} subtypes</li>
 *   <li>Spring Validation ({@link MethodArgumentNotValidException}, {@link ConstraintViolationException})</li>
 *   <li>Spring Security ({@link AccessDeniedException}, {@link BadCredentialsException})</li>
 *   <li>Spring MVC errors (method not allowed, malformed JSON, type mismatches)</li>
 *   <li>Catch-all for unexpected exceptions (returns 500 without leaking internals)</li>
 * </ul>
 * </p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================================================================
    // AquaTrack Custom Exceptions
    // =========================================================================

    /**
     * Handles all custom {@link AquaTrackException} subtypes.
     *
     * <p>Each subclass carries its own {@link HttpStatus}, so this single
     * handler covers ResourceNotFoundException (404), BadRequestException (400),
     * TenantAccessException (403), etc.</p>
     */
    @ExceptionHandler(AquaTrackException.class)
    public ResponseEntity<ErrorResponse> handleAquaTrackException(
            AquaTrackException ex, HttpServletRequest request) {

        log.error("AquaTrack exception [{}] at {}: {}",
                ex.getHttpStatus(), request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(ex.getHttpStatus().value())
                .error(ex.getHttpStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    // =========================================================================
    // Validation Exceptions
    // =========================================================================

    /**
     * Handles @Valid/@Validated failures on @RequestBody DTOs.
     *
     * <p>Collects all field-level violations into a map so the client
     * receives all errors in a single response, not just the first.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Build field → message map for all violations
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(objError -> {
            if (objError instanceof FieldError fieldError) {
                validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                validationErrors.put(objError.getObjectName(), objError.getDefaultMessage());
            }
        });

        log.warn("Validation failed at {}: {}", request.getRequestURI(), validationErrors);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed. Please check the 'validationErrors' field.")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles @Validated failures on path/query parameters and service-layer constraints.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            // Extract the field name from the full property path
            String field = violation.getPropertyPath().toString();
            String fieldName = field.contains(".") ? field.substring(field.lastIndexOf('.') + 1) : field;
            validationErrors.put(fieldName, violation.getMessage());
        }

        log.warn("Constraint violation at {}: {}", request.getRequestURI(), validationErrors);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("Request constraint violation. Please check the 'validationErrors' field.")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    // =========================================================================
    // Spring Security Exceptions
    // =========================================================================

    /**
     * Handles access denied when an authenticated user lacks permission.
     * Maps to HTTP 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("You do not have permission to perform this action.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handles invalid credentials during login.
     * Maps to HTTP 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Bad credentials attempt at {}", request.getRequestURI());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Invalid email or password.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles disabled user account access attempts.
     * Maps to HTTP 401 Unauthorized.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledUser(
            DisabledException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Account Disabled")
                .message("Your account is disabled. Please verify your email or contact the administrator.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles locked user account access attempts.
     * Maps to HTTP 401 Unauthorized.
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedAccount(
            LockedException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Account Locked")
                .message("Your account has been temporarily locked due to multiple failed login attempts. Please try again later.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // =========================================================================
    // Spring MVC Exceptions
    // =========================================================================

    /**
     * Handles malformed JSON in request body.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed JSON at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Malformed Request")
                .message("The request body could not be parsed. Please ensure your JSON is valid.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles unsupported HTTP methods.
     * Maps to HTTP 405 Method Not Allowed.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message(String.format("HTTP method '%s' is not supported for this endpoint.", ex.getMethod()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    /**
     * Handles missing required query parameters.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Missing Parameter")
                .message(String.format("Required parameter '%s' of type '%s' is missing.",
                        ex.getParameterName(), ex.getParameterType()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles type conversion failures in path variables and query parameters.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format(
                "Parameter '%s' has invalid value '%s'. Expected type: %s.",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Type Mismatch")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    // =========================================================================
    // Catch-All
    // =========================================================================

    /**
     * Catch-all handler for any unexpected exception not covered above.
     *
     * <p>Returns HTTP 500 with a generic message. The actual exception details
     * are logged server-side but never exposed to the client to prevent
     * information leakage.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        // Log the full stack trace server-side
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please contact support if the problem persists.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.internalServerError().body(error);
    }
}
