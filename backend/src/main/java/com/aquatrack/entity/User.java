package com.aquatrack.entity;

import com.aquatrack.entity.base.BaseEntity;
import com.aquatrack.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Core user entity for AquaTrack's multi-tenant authentication system.
 *
 * <p>Implements {@link UserDetails} so Spring Security can load users
 * directly from the repository without a separate {@code UserDetailsService}
 * adapter (though we still have one for the service boundary).</p>
 *
 * <p>Multi-tenancy: every non-SUPER_ADMIN user belongs to exactly one
 * apartment society via {@code apartmentId}. All queries involving this
 * entity MUST filter by {@code apartmentId} unless the caller holds
 * the SUPER_ADMIN role.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {

    // ── Tenant / Household scope ──────────────────────────────

    /**
     * The apartment society this user belongs to.
     * NULL only for SUPER_ADMIN users.
     * Embedded in the JWT as the {@code apartmentId} claim.
     */
    @Column(name = "apartment_id")
    private UUID apartmentId;

    /**
     * The household this resident is assigned to.
     * Only populated for users with the RESIDENT role.
     */
    @Column(name = "household_id")
    private UUID householdId;

    // ── Role ─────────────────────────────────────────────────

    /**
     * The user's role — determines their permissions.
     * Eager-fetched because it is needed on every authenticated request.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_users_role"))
    private Role role;

    // ── Identity ─────────────────────────────────────────────

    /** Primary login credential and unique identifier. */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /** BCrypt-hashed password. Never store plaintext. */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 30)
    private Gender gender;

    @Column(name = "profile_photo_url", length = 1000)
    private String profilePhotoUrl;

    // ── Account lifecycle ─────────────────────────────────────

    /** False until the user clicks the verification link in their welcome email. */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = Boolean.FALSE;

    /**
     * When true, the account is temporarily locked due to too many
     * failed login attempts. Checked by Spring Security via {@link #isAccountNonLocked()}.
     */
    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private Boolean accountLocked = Boolean.FALSE;

    /**
     * Timestamp when the account lock expires.
     * NULL if the account is not locked or was manually unlocked by an admin.
     */
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    /**
     * Consecutive failed login attempts since the last successful login.
     * Resets to 0 on successful login.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /** Timestamp of the last password change — used to invalidate older tokens. */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // ── Computed fields ───────────────────────────────────────

    /**
     * Returns the user's full name (first + last).
     *
     * @return full name as a single string
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ── Spring Security UserDetails implementation ────────────

    /**
     * Returns the single granted authority derived from this user's role.
     * Spring Security uses this for {@code hasRole()} and {@code hasAuthority()} checks.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getName().getSpringSecurityName()));
    }

    /** Returns the BCrypt-hashed password — used by Spring Security's DaoAuthenticationProvider. */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** Returns the email address as the login username. */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Account is non-expired if it is not soft-deleted.
     * We use {@code status} and {@code isDeleted} for lifecycle management instead.
     */
    @Override
    public boolean isAccountNonExpired() {
        return Boolean.FALSE.equals(getIsDeleted());
    }

    /**
     * Account is non-locked if:
     * 1. {@code accountLocked} flag is false, OR
     * 2. The lock has expired (accountLockedUntil is in the past).
     */
    @Override
    public boolean isAccountNonLocked() {
        if (Boolean.FALSE.equals(accountLocked)) {
            return true;
        }
        // Auto-unlock if the lock duration has passed
        if (accountLockedUntil != null && LocalDateTime.now().isAfter(accountLockedUntil)) {
            return true;
        }
        return false;
    }

    /**
     * Credentials are non-expired — we track password age via {@code passwordChangedAt}
     * but do not enforce expiry through Spring Security's mechanism.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Account is enabled if it is not soft-deleted.
     * Note: email_verified is NOT checked here — it is checked explicitly in AuthService
     * to provide a more specific error message to the user.
     */
    @Override
    public boolean isEnabled() {
        return Boolean.FALSE.equals(getIsDeleted());
    }
}
