package com.aquatrack.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints that support pagination.
 *
 * <p>Wraps a Spring Data {@link Page} into a consistent JSON shape that
 * includes content, pagination metadata, and sorting information.</p>
 *
 * <p>Response shape:
 * <pre>{@code
 * {
 *   "content": [ ... ],
 *   "pageNumber": 0,
 *   "pageSize": 20,
 *   "totalElements": 150,
 *   "totalPages": 8,
 *   "first": true,
 *   "last": false,
 *   "hasNext": true,
 *   "hasPrevious": false
 * }
 * }</pre>
 * </p>
 *
 * @param <T> the type of items in the page
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Paginated response with metadata")
public class PagedResponse<T> {

    /** The items on this page. */
    @Schema(description = "List of items on the current page")
    private List<T> content;

    /** Zero-based current page number. */
    @Schema(description = "Current page number (0-based)", example = "0")
    private int pageNumber;

    /** Number of items requested per page. */
    @Schema(description = "Page size", example = "20")
    private int pageSize;

    /** Total number of items across all pages. */
    @Schema(description = "Total number of elements", example = "150")
    private long totalElements;

    /** Total number of pages available. */
    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;

    /** Whether this is the first page. */
    @Schema(description = "True if this is the first page", example = "true")
    private boolean first;

    /** Whether this is the last page. */
    @Schema(description = "True if this is the last page", example = "false")
    private boolean last;

    /** Whether there is a next page available. */
    @Schema(description = "True if a next page exists", example = "true")
    private boolean hasNext;

    /** Whether there is a previous page available. */
    @Schema(description = "True if a previous page exists", example = "false")
    private boolean hasPrevious;

    // =========================================================================
    // Factory Method
    // =========================================================================

    /**
     * Builds a {@code PagedResponse} from a Spring Data {@link Page} object.
     *
     * <p>This is the primary constructor — always prefer this over building manually.</p>
     *
     * @param page the Spring Data page result
     * @param <T>  the content type
     * @return a populated {@code PagedResponse}
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
