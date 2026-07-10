package com.aquatrack.dto.response;

import com.aquatrack.enums.Gender;
import com.aquatrack.enums.Status;
import com.aquatrack.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Full user profile response returned by the Users API.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full user profile")
public class UserResponse {

    @Schema(description = "User UUID")
    private UUID id;

    @Schema(description = "Apartment society UUID")
    private UUID apartmentId;

    @Schema(description = "Assigned household UUID (residents only)")
    private UUID householdId;

    @Schema(description = "Assigned role")
    private UserRole role;

    @Schema(description = "Role display name", example = "Administrator")
    private String roleDisplayName;

    @Schema(description = "First name", example = "Priya")
    private String firstName;

    @Schema(description = "Last name", example = "Sharma")
    private String lastName;

    @Schema(description = "Full name", example = "Priya Sharma")
    private String fullName;

    @Schema(description = "Email address", example = "priya.sharma@example.com")
    private String email;

    @Schema(description = "Phone number", example = "+91-9876543210")
    private String phoneNumber;

    @Schema(description = "Gender")
    private Gender gender;

    @Schema(description = "Profile photo URL")
    private String profilePhotoUrl;

    @Schema(description = "Whether the email has been verified")
    private Boolean emailVerified;

    @Schema(description = "Whether the account is locked")
    private Boolean accountLocked;

    @Schema(description = "Account status")
    private Status status;

    @Schema(description = "Last successful login timestamp")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
