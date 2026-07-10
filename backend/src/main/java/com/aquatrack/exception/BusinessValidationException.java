package com.aquatrack.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a business rule validation fails that is not covered by
 * Jakarta Bean Validation annotations.
 *
 * <p>Examples:
 * <ul>
 *   <li>Meter reading is less than the previous reading</li>
 *   <li>Billing cycle overlaps with an existing cycle</li>
 *   <li>Invoice generated for a period with missing readings</li>
 * </ul>
 * </p>
 *
 * <p>Maps to HTTP 422 Unprocessable Entity.</p>
 */
public class BusinessValidationException extends AquaTrackException {

    public BusinessValidationException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
