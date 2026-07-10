package com.aquatrack.service;

import com.aquatrack.entity.User;

/**
 * Contract for all transactional email operations in AquaTrack.
 *
 * <p>All methods are fire-and-forget — implementations send emails
 * asynchronously via {@code @Async} so the calling thread is never blocked.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public interface EmailService {

    /**
     * Sends a welcome + email verification link to a newly registered user.
     *
     * @param user  the recipient user entity
     * @param token the raw verification token to embed in the link
     */
    void sendVerificationEmail(User user, String token);

    /**
     * Sends a password reset link to the user.
     *
     * @param user  the recipient user entity
     * @param token the raw reset token to embed in the link
     */
    void sendPasswordResetEmail(User user, String token);

    /**
     * Sends a notification when the user's password is changed successfully.
     *
     * @param user the user whose password was changed
     */
    void sendPasswordChangedNotification(User user);

    /**
     * Sends a welcome email after the user's account is fully activated
     * (email verified and account approved by admin if required).
     *
     * @param user the newly activated user
     */
    void sendWelcomeEmail(User user);

    /**
     * Sends an account-locked notification with instructions to unlock.
     *
     * @param user         the locked user
     * @param lockDuration how long the account will remain locked (in minutes)
     */
    void sendAccountLockedNotification(User user, int lockDuration);
}
