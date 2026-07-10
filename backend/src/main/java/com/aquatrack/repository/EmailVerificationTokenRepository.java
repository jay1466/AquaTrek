package com.aquatrack.repository;

import com.aquatrack.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link EmailVerificationToken} entity.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    /**
     * Finds a valid, unused verification token by its raw value.
     *
     * @param token the raw token string from the email link
     * @return the token entity if found, unused, and not deleted
     */
    @Query("""
            SELECT t FROM EmailVerificationToken t
            WHERE t.token = :token
              AND t.used = FALSE
              AND t.isDeleted = FALSE
            """)
    Optional<EmailVerificationToken> findValidByToken(@Param("token") String token);

    /**
     * Invalidates all unused verification tokens for a user before issuing a new one.
     * Prevents multiple concurrent valid verification links from existing.
     *
     * @param userId the user's ID
     */
    @Modifying
    @Query("""
            UPDATE EmailVerificationToken t
            SET t.used = TRUE, t.isDeleted = TRUE
            WHERE t.user.id = :userId
              AND t.used = FALSE
            """)
    void invalidatePreviousTokensForUser(@Param("userId") UUID userId);
}
