package com.aquatrack.entity;

import com.aquatrack.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One-time token sent to a user's email address to verify ownership.
 *
 * <p>A new token is generated on registration and on "Resend Verification"
 * requests. Previous unused tokens for the same user are invalidated
 * before issuing a new one.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(name = "email_verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_email_verification_user"))
    private User user;

    /** Raw secure random token — included in the verification email link. */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = Boolean.FALSE;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Marks this token as used.
     * Should be called within the same transaction that sets emailVerified=true on the user.
     */
    public void markUsed() {
        this.used = Boolean.TRUE;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Checks whether this token is still valid.
     *
     * @return true if not used and not expired
     */
    public boolean isValid() {
        return Boolean.FALSE.equals(used) && LocalDateTime.now().isBefore(expiresAt);
    }
}
