package com.aquatrack.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an attempt is made to create a resource that already exists
 * (e.g., registering an email that is already in use).
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends AquaTrackException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.CONFLICT
        );
    }

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
