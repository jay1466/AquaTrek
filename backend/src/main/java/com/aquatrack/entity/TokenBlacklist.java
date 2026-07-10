package com.aquatrack.entity;

import com.aquatrack.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores revoked JWT access tokens to prevent reuse after logout.
 *
 * <p>On logout, the current access token's {@code jti} (JWT ID) claim
 * is inserted here. The {@link com.aquatrack.security.filter.JwtAuthFilter}
 * checks this table on every request to reject blacklisted tokens.</p>
 *
 * <p>A scheduled task removes entries whose {@code expiresAt} has passed,
 * keeping the table small and lookups fast.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist extends BaseEntity {

    /** The unique ID of the JWT token ("jti" claim). */
    @Column(name = "token_jti", nullable = false, unique = true, length = 255)
    private String tokenJti;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "revoked_at", nullable = false)
    @Builder.Default
    private LocalDateTime revokedAt = LocalDateTime.now();

    /** Copied from the JWT exp claim — allows expired entries to be cleaned up. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Reason for blacklisting: LOGOUT | PASSWORD_CHANGE | ADMIN_REVOKE */
    @Column(name = "reason", length = 255)
    private String reason;
}
