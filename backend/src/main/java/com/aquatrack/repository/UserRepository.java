package com.aquatrack.repository;

import com.aquatrack.entity.User;
import com.aquatrack.enums.Status;
import com.aquatrack.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entity.
 *
 * <p>All query methods that access tenant data include an {@code apartmentId}
 * parameter to enforce multi-tenant isolation at the query level.
 * Never use methods that omit {@code apartmentId} in business logic
 * (only SUPER_ADMIN operations may do so).</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ── Email Lookup (used during login — no tenant scope needed) ──

    /**
     * Finds an active, non-deleted user by email address.
     * Used by the authentication filter and login flow.
     *
     * @param email the user's email address
     * @return the user if found and not deleted
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * Checks whether an email address is already registered (any tenant).
     *
     * @param email the email to check
     * @return true if a non-deleted user with this email exists
     */
    boolean existsByEmailAndIsDeletedFalse(String email);

    // ── Tenant-scoped queries ──────────────────────────────────────

    /**
     * Finds a user within a specific apartment by their ID.
     * Use this instead of {@link #findById(Object)} in all service methods.
     *
     * @param id          the user UUID
     * @param apartmentId the calling user's apartment ID
     * @return the user if found in this tenant and not deleted
     */
    Optional<User> findByIdAndApartmentIdAndIsDeletedFalse(UUID id, UUID apartmentId);

    /**
     * Lists all active users within an apartment society, with pagination.
     *
     * @param apartmentId the apartment ID to scope results to
     * @param pageable    pagination and sorting parameters
     * @return a page of users in the tenant
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.apartmentId = :apartmentId
              AND u.isDeleted = FALSE
            """)
    Page<User> findAllByApartmentId(@Param("apartmentId") UUID apartmentId, Pageable pageable);

    /**
     * Lists all users within an apartment by role.
     *
     * @param apartmentId the tenant ID
     * @param roleName    the role to filter by
     * @param pageable    pagination parameters
     * @return matching users
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.role r
            WHERE u.apartmentId = :apartmentId
              AND r.name = :roleName
              AND u.isDeleted = FALSE
            """)
    Page<User> findByApartmentIdAndRole(
            @Param("apartmentId") UUID apartmentId,
            @Param("roleName") UserRole roleName,
            Pageable pageable
    );

    /**
     * Full-text search within an apartment — matches first name, last name, or email.
     *
     * @param apartmentId the tenant ID
     * @param keyword     the search term (case-insensitive)
     * @param pageable    pagination parameters
     * @return matching users
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.apartmentId = :apartmentId
              AND u.isDeleted = FALSE
              AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<User> searchByApartmentId(
            @Param("apartmentId") UUID apartmentId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Counts users in an apartment by status (for dashboard widgets).
     *
     * @param apartmentId the tenant ID
     * @param status      the status to count
     * @return number of matching users
     */
    long countByApartmentIdAndStatusAndIsDeletedFalse(UUID apartmentId, Status status);

    // ── Account Management ─────────────────────────────────────────

    /**
     * Updates failed login attempt count and optional lock fields directly
     * without loading the full User entity (avoids optimistic lock conflicts).
     *
     * @param userId              the user's ID
     * @param failedAttempts      the new failure count
     * @param accountLocked       whether the account should be locked
     * @param accountLockedUntil  expiry of the lock (null = not locked)
     */
    @Modifying
    @Query("""
            UPDATE User u
            SET u.failedLoginAttempts = :failedAttempts,
                u.accountLocked = :accountLocked,
                u.accountLockedUntil = :accountLockedUntil
            WHERE u.id = :userId
            """)
    void updateLoginLockStatus(
            @Param("userId") UUID userId,
            @Param("failedAttempts") int failedAttempts,
            @Param("accountLocked") boolean accountLocked,
            @Param("accountLockedUntil") LocalDateTime accountLockedUntil
    );

    /**
     * Resets the failed login counter and clears any account lock on successful login.
     *
     * @param userId    the user's ID
     * @param loginTime the timestamp of the successful login
     * @param loginIp   the IP address of the successful login
     */
    @Modifying
    @Query("""
            UPDATE User u
            SET u.failedLoginAttempts = 0,
                u.accountLocked = FALSE,
                u.accountLockedUntil = NULL,
                u.lastLoginAt = :loginTime,
                u.lastLoginIp = :loginIp
            WHERE u.id = :userId
            """)
    void recordSuccessfulLogin(
            @Param("userId") UUID userId,
            @Param("loginTime") LocalDateTime loginTime,
            @Param("loginIp") String loginIp
    );

    /**
     * Marks a user's email as verified and sets their status to ACTIVE.
     *
     * @param userId the user's ID
     */
    @Modifying
    @Query("""
            UPDATE User u
            SET u.emailVerified = TRUE,
                u.status = 'ACTIVE'
            WHERE u.id = :userId
            """)
    void markEmailVerified(@Param("userId") UUID userId);

    /**
     * Updates a user's password hash and records the change timestamp.
     *
     * @param userId      the user's ID
     * @param newHash     the new BCrypt password hash
     * @param changedAt   the timestamp of the change
     */
    @Modifying
    @Query("""
            UPDATE User u
            SET u.passwordHash = :newHash,
                u.passwordChangedAt = :changedAt
            WHERE u.id = :userId
            """)
    void updatePassword(
            @Param("userId") UUID userId,
            @Param("newHash") String newHash,
            @Param("changedAt") LocalDateTime changedAt
    );
}
