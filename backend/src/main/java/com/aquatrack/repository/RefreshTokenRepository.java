package com.aquatrack.repository;

import com.aquatrack.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link RefreshToken} entity.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds a valid (non-revoked, non-expired) refresh token by its hash.
     *
     * @param tokenHash the SHA-256 hash of the raw refresh token
     * @return the matching token if found and valid
     */
    @Query("""
            SELECT rt FROM RefreshToken rt
            WHERE rt.tokenHash = :tokenHash
              AND rt.revoked = FALSE
              AND rt.expiresAt > :now
              AND rt.isDeleted = FALSE
            """)
    Optional<RefreshToken> findValidByTokenHash(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now
    );

    /**
     * Revokes all active refresh tokens for a specific user.
     * Called on logout (revokes all sessions) or password change.
     *
     * @param userId       the user's ID
     * @param reason       the revocation reason
     * @param revokedAt    the timestamp of revocation
     */
    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revoked = TRUE,
                rt.revokedAt = :revokedAt,
                rt.revokedReason = :reason
            WHERE rt.user.id = :userId
              AND rt.revoked = FALSE
            """)
    void revokeAllByUserId(
            @Param("userId") UUID userId,
            @Param("reason") String reason,
            @Param("revokedAt") LocalDateTime revokedAt
    );

    /**
     * Deletes expired and revoked refresh tokens older than the given cutoff.
     * Called by a scheduled cleanup job to keep the table lean.
     *
     * @param cutoff delete tokens that expired before this timestamp
     * @return number of deleted rows
     */
    @Modifying
    @Query("""
            DELETE FROM RefreshToken rt
            WHERE rt.expiresAt < :cutoff
               OR rt.revoked = TRUE
            """)
    int deleteExpiredAndRevoked(@Param("cutoff") LocalDateTime cutoff);

    /** Counts active sessions for a user (for security monitoring). */
    long countByUserIdAndRevokedFalseAndExpiresAtAfter(UUID userId, LocalDateTime now);
}
