package com.aquatrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request body for initiating the forgot-password flow. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Forgot password request — sends a reset email")
public class ForgotPasswordRequest {

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    @Schema(description = "Email address of the account to reset", example = "priya.sharma@example.com")
    private String email;
}
