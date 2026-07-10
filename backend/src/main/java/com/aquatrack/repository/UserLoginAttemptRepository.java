package com.aquatrack.repository;

import com.aquatrack.entity.UserLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository for {@link UserLoginAttempt} entity.
 * Append-only audit log of all login attempts.
 */
@Repository
public interface UserLoginAttemptRepository extends JpaRepository<UserLoginAttempt, UUID> {

    /**
     * Counts consecutive failed login attempts for an email within a time window.
     * Used to decide whether to lock the account.
     *
     * @param email the email address to check
     * @param since count attempts after this timestamp
     * @return number of failed attempts
     */
    @Query("""
            SELECT COUNT(a) FROM UserLoginAttempt a
            WHERE a.email = :email
              AND a.success = FALSE
              AND a.attemptedAt > :since
            """)
    long countFailedAttemptsSince(
            @Param("email") String email,
            @Param("since") LocalDateTime since
    );
}
