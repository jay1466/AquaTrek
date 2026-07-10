package com.aquatrack.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Universal API response wrapper for all AquaTrack endpoints.
 *
 * <p>Every controller method returns an {@code ApiResponse<T>} to ensure
 * consistent response structure across the entire API. Consumers always
 * get the same envelope regardless of the endpoint.</p>
 *
 * <p>Successful response shape:
 * <pre>{@code
 * {
 *   "success": true,
 *   "message": "Apartment created successfully.",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * }</pre>
 * </p>
 *
 * <p>Error response shape:
 * <pre>{@code
 * {
 *   "success": false,
 *   "message": "Apartment not found with id: abc-123",
 *   "data": null,
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * }</pre>
 * </p>
 *
 * @param <T> the type of the payload data
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response envelope")
public class ApiResponse<T> {

    /** Whether the operation completed successfully. */
    @Schema(description = "Indicates if the request was successful", example = "true")
    private boolean success;

    /** Human-readable message describing the outcome. */
    @Schema(description = "Human-readable result message", example = "Resource created successfully.")
    private String message;

    /** The actual response payload. Null on error responses. */
    @Schema(description = "Response payload (null on error)")
    private T data;

    /** Server-side timestamp when this response was generated. */
    @Schema(description = "Response generation timestamp", example = "2024-01-15T10:30:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // =========================================================================
    // Factory Methods
    // =========================================================================

    /**
     * Creates a successful response with both data and a message.
     *
     * @param data    the response payload
     * @param message a human-readable success message
     * @param <T>     the payload type
     * @return a successful {@code ApiResponse}
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with data and a default "Success." message.
     *
     * @param data the response payload
     * @param <T>  the payload type
     * @return a successful {@code ApiResponse}
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success.");
    }

    /**
     * Creates a successful response with only a message (no payload).
     * Use for operations like delete where no data is returned.
     *
     * @param message a human-readable success message
     * @param <T>     the payload type (typically {@code Void})
     * @return a successful {@code ApiResponse} with null data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with a message and no data.
     *
     * @param message a human-readable error message
     * @param <T>     the payload type
     * @return a failed {@code ApiResponse}
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with both a message and error detail data.
     *
     * @param message the error message
     * @param data    structured error details (e.g. validation error map)
     * @param <T>     the error data type
     * @return a failed {@code ApiResponse} with error detail
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
