package com.aquatrack.utility;

import com.aquatrack.response.ApiResponse;
import com.aquatrack.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.aquatrack.constants.AppConstants.*;

/**
 * Utility methods for constructing HTTP {@link ResponseEntity} objects
 * consistently across all AquaTrack controllers.
 *
 * <p>Controllers should use these helpers rather than constructing
 * {@link ResponseEntity} and {@link ApiResponse} objects inline.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public final class ResponseUtils {

    private ResponseUtils() {
        throw new UnsupportedOperationException("ResponseUtils is a utility class.");
    }

    // =========================================================================
    // Success Responses
    // =========================================================================

    /**
     * Returns HTTP 200 OK with data and message.
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * Returns HTTP 200 OK with data and a default "Success." message.
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Returns HTTP 200 OK with only a message (no payload body).
     * Use for delete and action-only operations.
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * Returns HTTP 201 Created with the newly created resource and message.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }

    /**
     * Returns HTTP 204 No Content (used for successful deletions with no body).
     */
    public static <T> ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Paged Responses
    // =========================================================================

    /**
     * Wraps a Spring Data {@link Page} into a paged {@link ApiResponse}.
     *
     * @param page    the Spring Data page result
     * @param message the success message
     * @param <T>     the content type
     * @return HTTP 200 with paged response body
     */
    public static <T> ResponseEntity<ApiResponse<PagedResponse<T>>> paged(
            Page<T> page, String message) {
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page), message));
    }

    // =========================================================================
    // Pagination Builder
    // =========================================================================

    /**
     * Builds a {@link Pageable} with validated page number, size, sort field, and direction.
     *
     * <p>Enforces:
     * <ul>
     *   <li>Page size cannot exceed {@link com.aquatrack.constants.AppConstants#MAX_PAGE_SIZE}</li>
     *   <li>Defaults to {@link com.aquatrack.constants.AppConstants#DEFAULT_SORT_BY} and DESC</li>
     * </ul>
     * </p>
     *
     * @param page      zero-based page number (null defaults to 0)
     * @param size      page size (null defaults to 20, max 100)
     * @param sortBy    field name to sort by (null defaults to "createdAt")
     * @param sortDir   "ASC" or "DESC" (null defaults to "DESC")
     * @return validated {@link Pageable}
     */
    public static Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE_NUMBER;
        int pageSize = (size != null && size > 0)
                ? Math.min(size, MAX_PAGE_SIZE)
                : DEFAULT_PAGE_SIZE;

        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_BY;

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
    }
}
