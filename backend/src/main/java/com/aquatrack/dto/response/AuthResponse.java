package com.aquatrack.dto.response;

import com.aquatrack.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response body returned after successful login or token refresh.
 *
 * <p>Contains both the access token (short-lived) and the refresh token
 * (long-lived). The frontend stores these and uses the refresh token
 * to silently renew the access token when it expires.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT tokens and user info")
public class AuthResponse {

    @Schema(description = "Short-lived JWT access token (15 min default)")
    private String accessToken;

    @Schema(description = "Long-lived refresh token (7 days default)")
    private String refreshToken;

    @Schema(description = "Token type — always Bearer", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token lifetime in milliseconds", example = "900000")
    private long expiresIn;

    @Schema(description = "Authenticated user's details")
    private UserSummary user;

    /**
     * Compact user summary embedded in the auth response.
     * Used by the frontend to display user info without an extra API call.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Authenticated user summary")
    public static class UserSummary {

        @Schema(description = "User UUID")
        private UUID id;

        @Schema(description = "Apartment society UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        private UUID apartmentId;

        @Schema(description = "User's full name", example = "Priya Sharma")
        private String fullName;

        @Schema(description = "Email address", example = "priya.sharma@example.com")
        private String email;

        @Schema(description = "Assigned role", example = "ADMIN")
        private UserRole role;

        @Schema(description = "Role display name", example = "Administrator")
        private String roleDisplayName;

        @Schema(description = "Profile photo URL")
        private String profilePhotoUrl;
    }
}
