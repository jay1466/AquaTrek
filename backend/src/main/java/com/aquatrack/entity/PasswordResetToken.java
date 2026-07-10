package com.aquatrack.entity;

import com.aquatrack.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One-time token for the "Forgot Password" flow.
 *
 * <p>Tokens are single-use and expire after 24 hours.
 * The IP address of the requester is stored for security audit purposes.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_password_reset_user"))
    private User user;

    /** Raw secure random token — included in the reset email link. */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = Boolean.FALSE;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "requested_ip", length = 45)
    private String requestedIp;

    /**
     * Marks this token as consumed.
     * Called within the same transaction as the password update.
     */
    public void markUsed() {
        this.used = Boolean.TRUE;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * @return true if the token has not been used and has not expired
     */
    public boolean isValid() {
        return Boolean.FALSE.equals(used) && LocalDateTime.now().isBefore(expiresAt);
    }
}
