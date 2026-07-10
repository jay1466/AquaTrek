package com.aquatrack.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource cannot be found in the database.
 *
 * <p>Maps to HTTP 404 Not Found.</p>
 *
 * <p>Usage example:
 * <pre>{@code
 * Apartment apartment = apartmentRepository.findById(id)
 *     .orElseThrow(() -> new ResourceNotFoundException("Apartment", "id", id));
 * }</pre>
 * </p>
 */
public class ResourceNotFoundException extends AquaTrackException {

    /**
     * Creates a not-found exception with a formatted message.
     *
     * @param resourceName the entity type (e.g., "Apartment", "Meter")
     * @param fieldName    the field used for lookup (e.g., "id", "serialNumber")
     * @param fieldValue   the value that was searched for
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.NOT_FOUND
        );
    }

    /**
     * Creates a not-found exception with a custom message.
     *
     * @param message custom error message
     */
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
