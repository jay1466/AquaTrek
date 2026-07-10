package com.aquatrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for user login.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login credentials")
public class LoginRequest {

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    @Schema(description = "Registered email address", example = "priya.sharma@example.com")
    private String email;

    @NotBlank(message = "Password is required.")
    @Schema(description = "Account password", example = "Secure@123")
    private String password;
}
