package com.aquatrack.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Structured error response body returned by the {@link com.aquatrack.exception.handler.GlobalExceptionHandler}.
 *
 * <p>Every API error is returned in this consistent format, allowing
 * frontend applications to handle errors uniformly.</p>
 *
 * <p>Example response for a validation error:
 * <pre>{@code
 * {
 *   "success": false,
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "path": "/api/v1/apartments",
 *   "timestamp": "2024-01-15T10:30:00",
 *   "validationErrors": {
 *     "name": "must not be blank",
 *     "contactEmail": "must be a valid email"
 *   }
 * }
 * }</pre>
 * </p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Structured error response body")
public class ErrorResponse {

    @Schema(description = "Always false for error responses", example = "false")
    @Builder.Default
    private boolean success = false;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP status reason phrase", example = "Not Found")
    private String error;

    @Schema(description = "Human-readable error message", example = "Apartment not found with id: 'abc-123'")
    private String message;

    @Schema(description = "The request path that caused the error", example = "/api/v1/apartments/abc-123")
    private String path;

    @Schema(description = "Server-side timestamp", example = "2024-01-15T10:30:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Field-level validation errors. Only present on 400/422 validation failures.
     * Map key = field name, value = violation message.
     */
    @Schema(description = "Field-level validation errors (present only for 400/422 responses)")
    private Map<String, String> validationErrors;
}
