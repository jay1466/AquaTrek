package com.aquatrack.repository;

import com.aquatrack.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository for {@link TokenBlacklist} entity.
 * Checked on every authenticated request to block revoked access tokens.
 */
@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, UUID> {

    /**
     * Checks if a JWT token (by its jti claim) has been blacklisted.
     * This query runs on EVERY authenticated request — it MUST use the index on token_jti.
     *
     * @param tokenJti the JWT's unique ID claim
     * @return true if the token has been blacklisted
     */
    boolean existsByTokenJti(String tokenJti);

    /**
     * Scheduled cleanup: removes blacklist entries for tokens that have already
     * expired naturally. Expired tokens can't be used even without the blacklist,
     * so they are safe to remove.
     *
     * @param cutoff entries that expired before this timestamp are deleted
     * @return number of deleted rows
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :cutoff")
    int deleteExpiredEntries(@Param("cutoff") LocalDateTime cutoff);
}
