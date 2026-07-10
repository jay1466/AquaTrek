package com.aquatrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for refreshing an access token using a valid refresh token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token request")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required.")
    @Schema(description = "The refresh token issued during login", example = "uuid-v4-refresh-token")
    private String refreshToken;
}
