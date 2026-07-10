package com.aquatrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request body for resetting a password using a one-time reset token. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password reset request using a one-time token")
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required.")
    @Schema(description = "The one-time token received in the reset email")
    private String token;

    @NotBlank(message = "New password is required.")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
    )
    @Schema(description = "The new password", example = "NewSecure@456")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required.")
    @Schema(description = "Must exactly match newPassword", example = "NewSecure@456")
    private String confirmPassword;
}
