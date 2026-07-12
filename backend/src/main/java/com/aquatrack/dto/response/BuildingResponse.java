package com.aquatrack.dto.response;

import com.aquatrack.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Building entities.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Building details")
public class BuildingResponse {

    @Schema(description = "Building UUID")
    private UUID id;

    @Schema(description = "Apartment UUID this building belongs to")
    private UUID apartmentId;

    @Schema(description = "Building name", example = "Tower A")
    private String name;

    @Schema(description = "Short code", example = "A")
    private String code;

    @Schema(description = "Number of floors")
    private Integer totalFloors;

    @Schema(description = "Total units in this building")
    private Integer totalUnits;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Building type: RESIDENTIAL | COMMERCIAL | MIXED")
    private String buildingType;

    @Schema(description = "Status")
    private Status status;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}
