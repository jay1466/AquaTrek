package com.aquatrack.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an unauthenticated request attempts to access a protected resource,
 * or when credentials are invalid.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends AquaTrackException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
