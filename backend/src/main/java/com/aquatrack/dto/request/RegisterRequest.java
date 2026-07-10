package com.aquatrack.dto.request;

import com.aquatrack.enums.Gender;
import com.aquatrack.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request body for user registration.
 *
 * <p>The {@code apartmentId} is required for all roles except SUPER_ADMIN.
 * The {@code role} field defaults to RESIDENT if not provided.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request")
public class RegisterRequest {

    @Schema(description = "Apartment society UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID apartmentId;

    @NotBlank(message = "First name is required.")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters.")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name may only contain letters, spaces, hyphens, and apostrophes.")
    @Schema(description = "User's first name", example = "Priya")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters.")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name may only contain letters, spaces, hyphens, and apostrophes.")
    @Schema(description = "User's last name", example = "Sharma")
    private String lastName;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    @Size(max = 255, message = "Email must not exceed 255 characters.")
    @Schema(description = "Email address (used as login)", example = "priya.sharma@example.com")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)."
    )
    @Schema(description = "Password (min 8 chars, must include upper, lower, digit, special char)", example = "Secure@123")
    private String password;

    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{7,20}$", message = "Please provide a valid phone number.")
    @Schema(description = "Contact phone number", example = "+91-9876543210")
    private String phoneNumber;

    @Schema(description = "Gender", example = "FEMALE")
    private Gender gender;

    @Schema(description = "Role to assign (defaults to RESIDENT if not provided)", example = "RESIDENT")
    private UserRole role;
}
