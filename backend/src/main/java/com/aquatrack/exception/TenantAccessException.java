package com.aquatrack.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a user attempts to access data belonging to a different
 * apartment society (tenant).
 *
 * <p>This is a critical security exception. Every service method that
 * retrieves tenant-scoped data must validate the apartment ID from the
 * JWT against the resource's apartment ID and throw this exception on mismatch.</p>
 *
 * <p>Maps to HTTP 403 Forbidden. The error message is intentionally vague
 * to avoid leaking information about the existence of resources in other tenants.</p>
 */
public class TenantAccessException extends AquaTrackException {

    /** Generic message to avoid information disclosure. */
    private static final String DEFAULT_MESSAGE =
            "Access denied. You do not have permission to access this resource.";

    public TenantAccessException() {
        super(DEFAULT_MESSAGE, HttpStatus.FORBIDDEN);
    }

    public TenantAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
