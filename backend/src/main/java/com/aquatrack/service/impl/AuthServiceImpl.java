package com.aquatrack.service.impl;

import com.aquatrack.constants.AppConstants;
import com.aquatrack.dto.request.*;
import com.aquatrack.dto.response.AuthResponse;
import com.aquatrack.dto.response.UserResponse;
import com.aquatrack.entity.*;
import com.aquatrack.enums.Status;
import com.aquatrack.enums.UserRole;
import com.aquatrack.exception.*;
import com.aquatrack.mapper.UserMapper;
import com.aquatrack.repository.*;
import com.aquatrack.security.jwt.JwtService;
import com.aquatrack.service.AuthService;
import com.aquatrack.service.EmailService;
import com.aquatrack.utility.AquaStringUtils;
import com.aquatrack.utility.TenantUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Implementation of {@link AuthService} — the complete authentication lifecycle.
 *
 * <p>Flow summary:
 * <ol>
 *   <li>Register → save user (PENDING), send verification email</li>
 *   <li>Verify Email → set status = ACTIVE, emailVerified = true</li>
 *   <li>Login → authenticate, check lock/verify state, issue tokens, record attempt</li>
 *   <li>Refresh → validate refresh token hash, rotate tokens</li>
 *   <li>Logout → blacklist access token jti, revoke all refresh tokens</li>
 *   <li>Forgot Password → generate reset token, send email</li>
 *   <li>Reset Password → validate token, update hash, revoke all sessions</li>
 *   <li>Change Password → verify current password, update, revoke sessions</li>
 * </ol>
 * </p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository                  userRepository;
    private final RoleRepository                  roleRepository;
    private final RefreshTokenRepository          refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository    passwordResetTokenRepository;
    private final TokenBlacklistRepository        tokenBlacklistRepository;
    private final UserLoginAttemptRepository      userLoginAttemptRepository;

    private final JwtService             jwtService;
    private final AuthenticationManager  authenticationManager;
    private final PasswordEncoder        passwordEncoder;
    private final EmailService           emailService;
    private final UserMapper             userMapper;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    // ── Registration ──────────────────────────────────────────

    /**
     * Registers a new user.
     *
     * <p>Steps:
     * <ol>
     *   <li>Validate email uniqueness</li>
     *   <li>Resolve the role (defaults to RESIDENT)</li>
     *   <li>Hash the password with BCrypt</li>
     *   <li>Save the user with PENDING status</li>
     *   <li>Generate an email verification token</li>
     *   <li>Send verification email asynchronously</li>
     * </ol>
     * </p>
     */
    @Override
    public String register(RegisterRequest request, HttpServletRequest httpRequest) {
        log.info("Processing registration for email: {}", request.getEmail());

        // Check email uniqueness across all tenants
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail().toLowerCase())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Resolve role — default to RESIDENT
        UserRole assignedRole = request.getRole() != null ? request.getRole() : UserRole.RESIDENT;

        // SUPER_ADMIN cannot be assigned via the API
        if (assignedRole == UserRole.SUPER_ADMIN) {
            throw new BadRequestException("The SUPER_ADMIN role cannot be assigned via the registration API.");
        }

        Role role = roleRepository.findByNameAndIsDeletedFalse(assignedRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", assignedRole));

        // Build and save the user
        User user = User.builder()
                .apartmentId(request.getApartmentId())
                .role(role)
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(AquaStringUtils.toTitleCase(request.getFirstName()))
                .lastName(AquaStringUtils.toTitleCase(request.getLastName()))
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())
                .emailVerified(false)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();
        user.setStatus(Status.PENDING);

        User savedUser = userRepository.save(user);
        log.info("User registered: {} [id={}]", savedUser.getEmail(), savedUser.getId());

        // Generate and send verification email
        sendVerificationEmail(savedUser);

        return "Registration successful! Please check your email to verify your account.";
    }

    // ── Login ─────────────────────────────────────────────────

    /**
     * Authenticates a user and issues JWT tokens.
     *
     * <p>The authentication flow:
     * <ol>
     *   <li>Load user by email — fail silently if not found (security)</li>
     *   <li>Check account lock expiry (auto-unlock if duration passed)</li>
     *   <li>Verify email confirmation</li>
     *   <li>Delegate credential check to Spring Security's AuthenticationManager</li>
     *   <li>On success: reset fail count, record login, issue tokens</li>
     *   <li>On failure: increment fail count, lock if threshold reached</li>
     * </ol>
     * </p>
     */
    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase().trim();
        String ip    = extractIp(httpRequest);
        String ua    = httpRequest.getHeader("User-Agent");

        log.info("Login attempt for email: {} from IP: {}", email, ip);

        // Load user — throw generic error to avoid email enumeration
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> {
                    recordLoginAttempt(email, ip, ua, false, "USER_NOT_FOUND");
                    return new BadCredentialsException("Invalid email or password.");
                });

        // Check account lock
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            if (user.getAccountLockedUntil() != null
                    && LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
                recordLoginAttempt(email, ip, ua, false, "ACCOUNT_LOCKED");
                throw new UnauthorizedException(
                        "Account is locked due to too many failed attempts. Please try again later or reset your password.");
            }
            // Auto-unlock: lock duration has expired
            userRepository.updateLoginLockStatus(user.getId(), 0, false, null);
        }

        // Check email verification
        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            recordLoginAttempt(email, ip, ua, false, "EMAIL_NOT_VERIFIED");
            throw new UnauthorizedException(
                    "Your email address has not been verified. Please check your inbox for the verification link.");
        }

        // Authenticate credentials via Spring Security
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (BadCredentialsException ex) {
            handleFailedLogin(user, ip, ua);
            throw new BadCredentialsException("Invalid email or password.");
        } catch (DisabledException ex) {
            recordLoginAttempt(email, ip, ua, false, "ACCOUNT_DISABLED");
            throw new UnauthorizedException("Your account is disabled. Please contact the administrator.");
        }

        // Successful login — reset fail count and record metadata
        userRepository.recordSuccessfulLogin(user.getId(), LocalDateTime.now(), ip);
        recordLoginAttempt(email, ip, ua, true, null);

        // Reload user after updates
        user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Issue tokens
        String accessToken  = jwtService.generateAccessToken(user);
        String rawRefresh   = jwtService.generateRawRefreshToken();

        saveRefreshToken(user, rawRefresh, ip, ua);

        log.info("Login successful for user: {} [id={}]", email, user.getId());

        return buildAuthResponse(accessToken, rawRefresh, user);
    }

    // ── Token Refresh ─────────────────────────────────────────

    /**
     * Validates the incoming refresh token, revokes it, and issues a new pair.
     * This is token rotation — every refresh invalidates the previous refresh token.
     */
    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String tokenHash = hashToken(request.getRefreshToken());
        String ip = extractIp(httpRequest);
        String ua = httpRequest.getHeader("User-Agent");

        // Find the valid, non-expired, non-revoked token
        RefreshToken stored = refreshTokenRepository
                .findValidByTokenHash(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException(
                        "Refresh token is invalid or has expired. Please log in again."));

        User user = stored.getUser();

        // Revoke the used token (rotation)
        stored.revoke("ROTATION");
        refreshTokenRepository.save(stored);

        // Issue new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRawRefresh  = jwtService.generateRawRefreshToken();
        saveRefreshToken(user, newRawRefresh, ip, ua);

        log.info("Token refreshed for user: {}", user.getEmail());
        return buildAuthResponse(newAccessToken, newRawRefresh, user);
    }

    // ── Logout ────────────────────────────────────────────────

    /**
     * Logs out by blacklisting the current access token and revoking all refresh tokens.
     */
    @Override
    public void logout(String accessToken) {
        try {
            String jti      = jwtService.extractJti(accessToken);
            UUID   userId   = jwtService.extractUserId(accessToken);
            LocalDateTime expiry = jwtService.extractExpiry(accessToken);

            // Blacklist the access token so it can't be reused
            TokenBlacklist entry = TokenBlacklist.builder()
                    .tokenJti(jti)
                    .userId(userId)
                    .revokedAt(LocalDateTime.now())
                    .expiresAt(expiry)
                    .reason("LOGOUT")
                    .build();
            tokenBlacklistRepository.save(entry);

            // Revoke all refresh tokens for this user (logs out all sessions)
            refreshTokenRepository.revokeAllByUserId(userId, "LOGOUT", LocalDateTime.now());

            log.info("User [id={}] logged out successfully.", userId);
        } catch (Exception e) {
            // Log but do not throw — logout should succeed even if token is malformed
            log.warn("Logout processing failed (non-critical): {}", e.getMessage());
        }
    }

    // ── Email Verification ────────────────────────────────────

    /** Verifies the user's email using the one-time token from the email link. */
    @Override
    public String verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findValidByToken(token)
                .orElseThrow(() -> new BadRequestException(
                        "The verification link is invalid or has already been used. Please request a new one."));

        if (!verificationToken.isValid()) {
            throw new BadRequestException(
                    "The verification link has expired. Please request a new verification email.");
        }

        // Mark token as used and activate the user
        verificationToken.markUsed();
        emailVerificationTokenRepository.save(verificationToken);

        userRepository.markEmailVerified(verificationToken.getUser().getId());

        log.info("Email verified for user: {}", verificationToken.getUser().getEmail());
        return "Email verified successfully! You can now log in.";
    }

    /** Re-sends the verification email. Invalidates any previous pending tokens first. */
    @Override
    public String resendVerificationEmail(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException("This email address is already verified.");
        }

        sendVerificationEmail(user);
        return "Verification email resent. Please check your inbox.";
    }

    // ── Forgot / Reset Password ───────────────────────────────

    /**
     * Initiates the forgot-password flow.
     *
     * <p>Always returns a success message regardless of whether the email exists
     * to prevent user enumeration attacks.</p>
     */
    @Override
    public String forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Forgot password requested for email: {}", email);

        // Silently ignore non-existent emails (security)
        userRepository.findByEmailAndIsDeletedFalse(email).ifPresent(user -> {
            // Invalidate any previous reset tokens
            passwordResetTokenRepository.invalidatePreviousTokensForUser(user.getId());

            // Generate new token
            String rawToken = AquaStringUtils.generateSecureToken();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(rawToken)
                    .expiresAt(LocalDateTime.now().plusHours(AppConstants.PASSWORD_RESET_EXPIRY_HOURS))
                    .requestedIp(extractIp(httpRequest))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user, rawToken);
            log.info("Password reset email sent to: {}", email);
        });

        return "If an account with that email exists, a password reset link has been sent.";
    }

    /** Resets the user's password using the one-time token. */
    @Override
    public String resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation password do not match.");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findValidByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException(
                        "The reset link is invalid or has expired. Please request a new one."));

        if (!resetToken.isValid()) {
            throw new BadRequestException("The reset link has expired. Please request a new password reset.");
        }

        User user = resetToken.getUser();

        // Prevent reuse of the same password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password cannot be the same as the current password.");
        }

        // Update password
        String newHash = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePassword(user.getId(), newHash, LocalDateTime.now());

        // Consume the reset token
        resetToken.markUsed();
        passwordResetTokenRepository.save(resetToken);

        // Revoke all sessions (security — new password means all old tokens should be invalid)
        refreshTokenRepository.revokeAllByUserId(user.getId(), "PASSWORD_CHANGE", LocalDateTime.now());

        log.info("Password reset completed for user: {}", user.getEmail());
        return "Password has been reset successfully. Please log in with your new password.";
    }

    /** Changes the password for the currently authenticated user. */
    @Override
    public String changePassword(ChangePasswordRequest request, String accessToken) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation password do not match.");
        }

        String email = TenantUtils.getCurrentUserEmail();
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password cannot be the same as the current password.");
        }

        String newHash = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePassword(user.getId(), newHash, LocalDateTime.now());

        // Blacklist current access token and revoke all refresh tokens
        logout(accessToken);

        log.info("Password changed for user: {}", email);
        return "Password changed successfully. Please log in with your new password.";
    }

    /** Returns the full profile of the currently authenticated user. */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String email = TenantUtils.getCurrentUserEmail();
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponse(user);
    }

    // ── Private Helpers ───────────────────────────────────────

    /**
     * Handles a failed login attempt: increments the counter and locks
     * the account if the maximum threshold is reached.
     */
    private void handleFailedLogin(User user, String ip, String ua) {
        int newFailCount = user.getFailedLoginAttempts() + 1;
        boolean shouldLock = newFailCount >= AppConstants.MAX_FAILED_LOGIN_ATTEMPTS;
        LocalDateTime lockUntil = shouldLock
                ? LocalDateTime.now().plusMinutes(AppConstants.ACCOUNT_LOCK_DURATION_MINUTES)
                : null;

        userRepository.updateLoginLockStatus(user.getId(), newFailCount, shouldLock, lockUntil);
        recordLoginAttempt(user.getEmail(), ip, ua, false, "INVALID_PASSWORD");

        if (shouldLock) {
            log.warn("Account locked for user: {} after {} failed attempts.", user.getEmail(), newFailCount);
        }
    }

    /**
     * Persists a {@link RefreshToken} record with the SHA-256 hash of the raw token.
     */
    private void saveRefreshToken(User user, String rawToken, String ip, String ua) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .apartmentId(user.getApartmentId())
                .tokenHash(hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .issuedIp(ip)
                .userAgent(ua)
                .build();
        refreshTokenRepository.save(token);
    }

    /**
     * Generates and saves an email verification token, then triggers async email send.
     */
    private void sendVerificationEmail(User user) {
        // Invalidate any existing unused tokens for this user
        emailVerificationTokenRepository.invalidatePreviousTokensForUser(user.getId());

        String rawToken = AquaStringUtils.generateSecureToken();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .token(rawToken)
                .expiresAt(LocalDateTime.now().plusHours(AppConstants.EMAIL_VERIFICATION_EXPIRY_HOURS))
                .build();
        emailVerificationTokenRepository.save(token);

        emailService.sendVerificationEmail(user, rawToken);
    }

    /** Records a login attempt to the audit log. */
    private void recordLoginAttempt(String email, String ip, String ua,
                                     boolean success, String reason) {
        UserLoginAttempt attempt = UserLoginAttempt.builder()
                .email(email)
                .ipAddress(ip)
                .userAgent(ua)
                .success(success)
                .failureReason(reason)
                .attemptedAt(LocalDateTime.now())
                .build();
        userLoginAttemptRepository.save(attempt);
    }

    /** Assembles the final {@link AuthResponse} from tokens and user entity. */
    private AuthResponse buildAuthResponse(String accessToken, String rawRefreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiryMs)
                .user(userMapper.toUserSummary(user))
                .build();
    }

    /**
     * Computes a SHA-256 hex hash of the raw token.
     * The raw token is never stored — only its hash.
     *
     * @param rawToken the plain refresh token string
     * @return hex-encoded SHA-256 hash
     */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Extracts the real client IP, honouring reverse-proxy headers.
     */
    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
