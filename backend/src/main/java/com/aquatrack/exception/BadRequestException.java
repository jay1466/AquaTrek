package com.aquatrack.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the client sends a request that is syntactically valid
 * but semantically incorrect (e.g., billing end date before start date).
 * Maps to HTTP 400 Bad Request.
 */
public class BadRequestException extends AquaTrackException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
}
