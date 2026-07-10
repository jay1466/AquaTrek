package com.aquatrack.entity;

import com.aquatrack.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persisted refresh token for AquaTrack's JWT rotation strategy.
 *
 * <p>On each token refresh, the current refresh token is revoked
 * and a new one is issued (token rotation). This limits the window
 * of exposure if a refresh token is compromised.</p>
 *
 * <p>The raw token value is NEVER stored — only its SHA-256 hash.
 * The raw value is returned to the client once on issuance and
 * never stored server-side.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_tokens_user"))
    private User user;

    /** The apartment society this token was issued for (mirrors the user's apartmentId). */
    @Column(name = "apartment_id")
    private UUID apartmentId;

    /** SHA-256 hash of the raw token string. Compared on each refresh request. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 500)
    private String tokenHash;

    /** Absolute expiry timestamp. After this, the token is invalid regardless of revocation. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Whether this token has been explicitly revoked (logout, rotation, or security event). */
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = Boolean.FALSE;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /** Reason for revocation: LOGOUT | ROTATION | SECURITY | PASSWORD_CHANGE */
    @Column(name = "revoked_reason", length = 255)
    private String revokedReason;

    /** IP address from which this token was originally issued. */
    @Column(name = "issued_ip", length = 45)
    private String issuedIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // ── Business methods ──────────────────────────────────────

    /**
     * Checks whether this refresh token is valid for use.
     *
     * @return true if the token has not been revoked and has not expired
     */
    public boolean isValid() {
        return Boolean.FALSE.equals(revoked) && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Revokes this refresh token with a reason.
     *
     * @param reason the reason for revocation (e.g., "LOGOUT", "ROTATION")
     */
    public void revoke(String reason) {
        this.revoked = Boolean.TRUE;
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = reason;
    }
}
