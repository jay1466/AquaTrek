package com.aquatrack.service;

import com.aquatrack.dto.request.*;
import com.aquatrack.dto.response.AuthResponse;
import com.aquatrack.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Contract for all authentication operations in AquaTrack.
 *
 * <p>Implementations handle the complete auth lifecycle:
 * register → verify email → login → refresh → logout → forgot/reset password.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public interface AuthService {

    /**
     * Registers a new user, sends a verification email, and returns a response
     * without issuing tokens (user must verify email first).
     *
     * @param request    the registration request DTO
     * @param httpRequest the HTTP request (for IP extraction)
     * @return message confirming registration and email dispatch
     */
    String register(RegisterRequest request, HttpServletRequest httpRequest);

    /**
     * Authenticates a user with email and password, records login metadata,
     * and issues a JWT access token + refresh token pair.
     *
     * @param request    the login credentials
     * @param httpRequest the HTTP request (for IP and user agent capture)
     * @return {@link AuthResponse} containing tokens and user summary
     */
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Silently renews the access token using a valid refresh token.
     * The old refresh token is revoked and a new one is issued (rotation).
     *
     * @param request    the refresh token request
     * @param httpRequest the HTTP request (for IP extraction)
     * @return new {@link AuthResponse} with rotated tokens
     */
    AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest);

    /**
     * Logs out the current user by blacklisting the access token
     * and revoking all their refresh tokens.
     *
     * @param accessToken the raw Bearer token extracted from the Authorization header
     */
    void logout(String accessToken);

    /**
     * Verifies a user's email address using the one-time token from the email link.
     *
     * @param token the raw verification token
     * @return confirmation message
     */
    String verifyEmail(String token);

    /**
     * Resends the email verification link to the user's registered email.
     *
     * @param email the user's email address
     * @return confirmation message
     */
    String resendVerificationEmail(String email);

    /**
     * Initiates the forgot-password flow by generating a reset token
     * and sending it to the user's email.
     *
     * @param request    the forgot password request
     * @param httpRequest the HTTP request (for IP audit)
     * @return a generic confirmation message (does not reveal whether email exists)
     */
    String forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest);

    /**
     * Resets the user's password using a valid one-time reset token.
     *
     * @param request the reset password request with token and new password
     * @return confirmation message
     */
    String resetPassword(ResetPasswordRequest request);

    /**
     * Changes the authenticated user's password after verifying the current one.
     *
     * @param request     the change password request
     * @param accessToken the current access token (to blacklist after password change)
     * @return confirmation message
     */
    String changePassword(ChangePasswordRequest request, String accessToken);

    /**
     * Returns the full profile of the currently authenticated user.
     *
     * @return {@link UserResponse} for the current user
     */
    UserResponse getCurrentUser();
}
