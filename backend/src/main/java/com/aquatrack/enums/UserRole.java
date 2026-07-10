package com.aquatrack.enums;

/**
 * Role definitions for AquaTrack's Role-Based Access Control (RBAC) system.
 *
 * <p>Every user in the system is assigned exactly one role. The role determines
 * which endpoints and resources the user may access.</p>
 *
 * <p>Role hierarchy (highest to lowest privilege):
 * <ol>
 *   <li>{@link #SUPER_ADMIN} — Anthropic/platform-level access (cross-tenant)</li>
 *   <li>{@link #ADMIN} — Apartment society administrator</li>
 *   <li>{@link #MANAGER} — Building/block manager within an apartment society</li>
 *   <li>{@link #RESIDENT} — Household resident (read-only, own data only)</li>
 * </ol>
 * </p>
 *
 * <p>In Spring Security, roles are stored as {@code ROLE_<name>} in the authorities.
 * The {@code @PreAuthorize("hasRole('ADMIN')")} annotation uses the value returned
 * by {@link #getSpringSecurityName()}.</p>
 */
public enum UserRole {

    /**
     * Platform-level super administrator.
     * Can access all apartment societies — used for platform support and operations.
     * This role is assigned only by direct database operations, never through the API.
     */
    SUPER_ADMIN("Super Admin", "ROLE_SUPER_ADMIN"),

    /**
     * Apartment society administrator.
     * Full access to all data within their apartment society.
     * Can manage buildings, households, meters, tariffs, invoices, and residents.
     */
    ADMIN("Administrator", "ROLE_ADMIN"),

    /**
     * Building or block manager.
     * Can view and manage households and meters within their assigned buildings.
     * Cannot modify tariffs, billing cycles, or apartment-level settings.
     */
    MANAGER("Manager", "ROLE_MANAGER"),

    /**
     * Household resident.
     * Read-only access to their own household's usage, invoices, and payments.
     * Cannot view other households' data.
     */
    RESIDENT("Resident", "ROLE_RESIDENT");

    /** Display name for UI rendering. */
    private final String displayName;

    /** Spring Security authority string (e.g., "ROLE_ADMIN"). */
    private final String springSecurityName;

    UserRole(String displayName, String springSecurityName) {
        this.displayName = displayName;
        this.springSecurityName = springSecurityName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSpringSecurityName() {
        return springSecurityName;
    }
}
