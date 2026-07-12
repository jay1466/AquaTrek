package com.aquatrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating or updating an apartment society.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Apartment create/update request")
public class ApartmentRequest {

    @NotBlank(message = "Apartment name is required.")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters.")
    @Schema(description = "Apartment society name", example = "Green Valley Society")
    private String name;

    @Size(max = 100, message = "Registration number must not exceed 100 characters.")
    @Schema(description = "Society registration number", example = "MH-2024-REG-0012")
    private String registrationNumber;

    @NotBlank(message = "Address line 1 is required.")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters.")
    @Schema(description = "Street address", example = "Plot No. 42, Sector 15")
    private String addressLine1;

    @Size(max = 255)
    @Schema(description = "Address line 2 (optional)", example = "Near City Mall")
    private String addressLine2;

    @NotBlank(message = "City is required.")
    @Size(max = 100)
    @Schema(description = "City", example = "Pune")
    private String city;

    @NotBlank(message = "State is required.")
    @Size(max = 100)
    @Schema(description = "State", example = "Maharashtra")
    private String state;

    @NotBlank(message = "Pincode is required.")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits.")
    @Schema(description = "6-digit pincode", example = "411015")
    private String pincode;

    @Size(max = 100)
    @Schema(description = "Country", example = "India")
    private String country;

    @Email(message = "Contact email must be a valid email address.")
    @Size(max = 255)
    @Schema(description = "Society contact email", example = "info@greenvalley.com")
    private String contactEmail;

    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{7,20}$", message = "Contact phone is not valid.")
    @Schema(description = "Society contact phone", example = "+91-20-2345-6789")
    private String contactPhone;

    @Size(max = 500)
    @Schema(description = "Website URL (optional)", example = "https://greenvalley.com")
    private String websiteUrl;

    @Size(max = 1000)
    @Schema(description = "Logo image URL (optional)")
    private String logoUrl;

    @Min(value = 0, message = "Total units must be 0 or greater.")
    @Schema(description = "Total number of units/flats", example = "120")
    private Integer totalUnits;

    @Min(value = 1900) @Max(value = 2100)
    @Schema(description = "Year the society was established", example = "2010")
    private Integer establishedYear;
}
