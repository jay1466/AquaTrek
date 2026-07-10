package com.aquatrack.exception;

import org.springframework.http.HttpStatus;

/**
 * Root exception for all AquaTrack-specific runtime exceptions.
 *
 * <p>All custom exceptions in the system extend this class.
 * It carries an {@link HttpStatus} that the {@link com.aquatrack.exception.handler.GlobalExceptionHandler}
 * uses to set the HTTP response status code, preventing the need for
 * status-code decisions scattered across service classes.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public class AquaTrackException extends RuntimeException {

    /** HTTP status code to be returned to the client. */
    private final HttpStatus httpStatus;

    /**
     * Creates a new exception with a message and HTTP status.
     *
     * @param message    human-readable error message
     * @param httpStatus the HTTP status code for this error
     */
    public AquaTrackException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    /**
     * Creates a new exception with a message, cause, and HTTP status.
     *
     * @param message    human-readable error message
     * @param cause      the underlying cause
     * @param httpStatus the HTTP status code for this error
     */
    public AquaTrackException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return the HTTP status code
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
