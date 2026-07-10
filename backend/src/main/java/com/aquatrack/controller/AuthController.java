package com.aquatrack.controller;

import com.aquatrack.constants.ApiConstants;
import com.aquatrack.constants.SecurityConstants;
import com.aquatrack.dto.request.*;
import com.aquatrack.dto.response.AuthResponse;
import com.aquatrack.dto.response.UserResponse;
import com.aquatrack.exception.BadRequestException;
import com.aquatrack.response.ApiResponse;
import com.aquatrack.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for all authentication operations.
 *
 * <p>Base path: {@code /api/v1/auth}
 *
 * <p>All endpoints in this controller are public EXCEPT:
 * <ul>
 *   <li>{@code POST /logout}      — requires a valid JWT Bearer token</li>
 *   <li>{@code GET  /me}          — requires a valid JWT Bearer token</li>
 *   <li>{@code POST /change-password} — requires a valid JWT Bearer token</li>
 * </ul>
 * </p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@RestController
@RequestMapping(ApiConstants.AUTH_BASE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Registration, login, token management, and password operations")
public class AuthController {

    private final AuthService authService;

    // ── Registration ──────────────────────────────────────────

    /**
     * Registers a new user and sends a verification email.
     *
     * @param request     validated registration body
     * @param httpRequest for IP capture
     * @return 201 Created with confirmation message
     */
    @PostMapping(ApiConstants.AUTH_REGISTER)
    @Operation(
        summary     = "Register a new user",
        description = "Creates a new user account and sends an email verification link. " +
                      "The user cannot log in until their email is verified."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
            description = "User registered — verification email sent"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
            description = "Validation errors"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
            description = "Email already in use")
    })
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("POST /auth/register — email: {}", request.getEmail());
        String message = authService.register(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message));
    }

    // ── Login ─────────────────────────────────────────────────

    /**
     * Authenticates credentials and returns JWT tokens.
     *
     * @param request     login credentials
     * @param httpRequest for IP and user-agent capture
     * @return 200 OK with access token, refresh token, and user summary
     */
    @PostMapping(ApiConstants.AUTH_LOGIN)
    @Operation(
        summary     = "Login with email and password",
        description = "Returns a JWT access token (15 min) and a refresh token (7 days). " +
                      "Store the refresh token securely and use it to obtain new access tokens silently."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
            description = "Invalid credentials, unverified email, or locked account"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429",
            description = "Too many login attempts — rate limit exceeded")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("POST /auth/login — email: {}", request.getEmail());
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful."));
    }

    // ── Token Refresh ─────────────────────────────────────────

    /**
     * Issues a new access token + refresh token pair using a valid refresh token.
     *
     * @param request     contains the raw refresh token
     * @param httpRequest for IP capture
     * @return 200 OK with new token pair
     */
    @PostMapping(ApiConstants.AUTH_REFRESH)
    @Operation(
        summary     = "Refresh access token",
        description = "Uses a valid refresh token to issue a new access + refresh token pair. " +
                      "The old refresh token is revoked (rotation). If the refresh token is expired " +
                      "or revoked, the user must log in again."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "Tokens refreshed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
            description = "Refresh token is invalid or expired")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthResponse response = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully."));
    }

    // ── Logout ────────────────────────────────────────────────

    /**
     * Logs out the current user by revoking the access and refresh tokens.
     *
     * @param httpRequest for Authorization header extraction
     * @return 200 OK with confirmation message
     */
    @PostMapping(ApiConstants.AUTH_LOGOUT)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary     = "Logout current user",
        description = "Blacklists the current access token and revokes all refresh tokens " +
                      "for the current user (all sessions). Requires a valid Bearer token."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "Logged out successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
            description = "No valid authentication token provided")
    })
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest httpRequest) {
        String token = extractBearerToken(httpRequest);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("You have been logged out successfully."));
    }

    // ── Email Verification ────────────────────────────────────

    /**
     * Verifies the user's email using the one-time token from the email link.
     *
     * @param token the raw verification token from the query parameter
     * @return 200 OK with confirmation
     */
    @GetMapping(ApiConstants.AUTH_VERIFY_EMAIL)
    @Operation(
        summary     = "Verify email address",
        description = "Activates the user's account using the token from the verification email. " +
                      "The link format is: GET /api/v1/auth/verify-email?token={token}"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "Email verified — account is now active"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
            description = "Token is invalid, already used, or expired")
    })
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @Parameter(description = "One-time verification token from the email link", required = true)
            @RequestParam String token
    ) {
        String message = authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * Resends the email verification link to the specified email.
     *
     * @param email the user's email address
     * @return 200 OK with confirmation
     */
    @PostMapping(ApiConstants.AUTH_RESEND_VERIFICATION)
    @Operation(
        summary     = "Resend email verification link",
        description = "Generates a new verification token and resends the verification email. " +
                      "Any previously issued unused tokens are invalidated."
    )
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @Parameter(description = "Email address to resend verification to", required = true)
            @RequestParam String email
    ) {
        String message = authService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // ── Forgot / Reset Password ───────────────────────────────

    /**
     * Initiates the forgot-password flow — sends a reset link to the email.
     *
     * @param request     contains the email address
     * @param httpRequest for IP audit logging
     * @return 200 OK with a generic message (does not reveal whether email exists)
     */
    @PostMapping(ApiConstants.AUTH_FORGOT_PASSWORD)
    @Operation(
        summary     = "Request a password reset email",
        description = "Sends a password reset link to the provided email address. " +
                      "Always returns 200 regardless of whether the email is registered " +
                      "to prevent user enumeration."
    )
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("POST /auth/forgot-password — email: {}", request.getEmail());
        String message = authService.forgotPassword(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * Resets the user's password using the one-time reset token.
     *
     * @param request contains the token and new password
     * @return 200 OK with confirmation
     */
    @PostMapping(ApiConstants.AUTH_RESET_PASSWORD)
    @Operation(
        summary     = "Reset password using token",
        description = "Resets the account password using the one-time token from the reset email. " +
                      "The token is invalidated after use. All existing sessions are revoked."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "Password reset successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
            description = "Token invalid/expired, passwords don't match, or new password same as current")
    })
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * Changes the password for the currently authenticated user.
     *
     * @param request     current and new password
     * @param httpRequest for Authorization header extraction
     * @return 200 OK with confirmation
     */
    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary     = "Change password (authenticated)",
        description = "Allows a logged-in user to change their password by providing their current password. " +
                      "All sessions are revoked after the change."
    )
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        String token  = extractBearerToken(httpRequest);
        String message = authService.changePassword(request, token);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // ── Current User ──────────────────────────────────────────

    /**
     * Returns the full profile of the currently authenticated user.
     *
     * @return 200 OK with {@link UserResponse}
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary     = "Get current user profile",
        description = "Returns the full profile of the currently authenticated user. " +
                      "Used by the frontend to hydrate the auth context after a page refresh."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "User profile returned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
            description = "No valid authentication")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // ── Private Helpers ───────────────────────────────────────

    /**
     * Extracts the raw Bearer token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the raw JWT string without the "Bearer " prefix
     * @throws BadRequestException if no Authorization header is present
     */
    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            throw new BadRequestException("Authorization header with Bearer token is required.");
        }
        return header.substring(SecurityConstants.BEARER_PREFIX.length());
    }
}
