package com.aquatrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request body for an authenticated user changing their own password. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change password for authenticated user")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required.")
    @Schema(description = "The user's current password")
    private String currentPassword;

    @NotBlank(message = "New password is required.")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase, lowercase, digit and special character."
    )
    @Schema(description = "The desired new password", example = "NewSecure@789")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required.")
    @Schema(description = "Must match newPassword exactly", example = "NewSecure@789")
    private String confirmPassword;
}
