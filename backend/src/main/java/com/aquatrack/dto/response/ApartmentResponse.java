package com.aquatrack.dto.response;

import com.aquatrack.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Apartment entities.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Apartment society details")
public class ApartmentResponse {

    @Schema(description = "Apartment UUID")
    private UUID id;

    @Schema(description = "Society name", example = "Green Valley Society")
    private String name;

    @Schema(description = "Registration number")
    private String registrationNumber;

    @Schema(description = "Address line 1")
    private String addressLine1;

    @Schema(description = "Address line 2")
    private String addressLine2;

    @Schema(description = "City")
    private String city;

    @Schema(description = "State")
    private String state;

    @Schema(description = "Pincode")
    private String pincode;

    @Schema(description = "Country")
    private String country;

    @Schema(description = "Full formatted address")
    private String fullAddress;

    @Schema(description = "Contact email")
    private String contactEmail;

    @Schema(description = "Contact phone")
    private String contactPhone;

    @Schema(description = "Website URL")
    private String websiteUrl;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "Total units")
    private Integer totalUnits;

    @Schema(description = "Total buildings")
    private Integer totalBuildings;

    @Schema(description = "Established year")
    private Integer establishedYear;

    @Schema(description = "Subscription plan")
    private String subscriptionPlan;

    @Schema(description = "Subscription valid until")
    private LocalDateTime subscriptionValidUntil;

    @Schema(description = "Status")
    private Status status;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}
