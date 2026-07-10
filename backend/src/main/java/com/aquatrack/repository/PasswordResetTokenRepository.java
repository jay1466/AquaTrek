package com.aquatrack.repository;

import com.aquatrack.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link PasswordResetToken} entity.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Finds a valid, unused password reset token.
     *
     * @param token the raw token string from the reset email link
     * @return the token entity if found, unused, not expired, and not deleted
     */
    @Query("""
            SELECT t FROM PasswordResetToken t
            WHERE t.token = :token
              AND t.used = FALSE
              AND t.isDeleted = FALSE
              AND t.expiresAt > CURRENT_TIMESTAMP
            """)
    Optional<PasswordResetToken> findValidByToken(@Param("token") String token);

    /**
     * Invalidates all outstanding reset tokens for a user before issuing a new one.
     *
     * @param userId the user's ID
     */
    @Modifying
    @Query("""
            UPDATE PasswordResetToken t
            SET t.used = TRUE, t.isDeleted = TRUE
            WHERE t.user.id = :userId
              AND t.used = FALSE
            """)
    void invalidatePreviousTokensForUser(@Param("userId") UUID userId);
}
