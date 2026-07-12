package com.aquatrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating or updating a building within an apartment society.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Building create/update request")
public class BuildingRequest {

    @NotBlank(message = "Building name is required.")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters.")
    @Schema(description = "Building display name", example = "Tower A")
    private String name;

    @NotBlank(message = "Building code is required.")
    @Size(min = 1, max = 50, message = "Code must be between 1 and 50 characters.")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$",
             message = "Code may only contain letters, digits, hyphens, and underscores.")
    @Schema(description = "Short code used in flat numbers", example = "A")
    private String code;

    @Min(value = 1, message = "Total floors must be at least 1.")
    @Max(value = 200, message = "Total floors must not exceed 200.")
    @Schema(description = "Number of floors in this building", example = "12")
    private Integer totalFloors;

    @Min(value = 0)
    @Schema(description = "Total units/flats in this building", example = "48")
    private Integer totalUnits;

    @Size(max = 1000)
    @Schema(description = "Optional description")
    private String description;

    @Schema(description = "Building type: RESIDENTIAL | COMMERCIAL | MIXED", example = "RESIDENTIAL")
    private String buildingType;
}
