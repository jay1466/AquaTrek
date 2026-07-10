package com.aquatrack.enums;

/**
 * Lifecycle status shared by all AquaTrack domain entities.
 *
 * <p>Stored as a VARCHAR in PostgreSQL. Used in combination with
 * {@code is_deleted} for full lifecycle tracking.</p>
 */
public enum Status {

    /** Record is operational and visible to all queries. */
    ACTIVE("Active"),

    /** Record is temporarily disabled but not deleted. */
    INACTIVE("Inactive"),

    /** Record is suspended due to a policy or payment violation. */
    SUSPENDED("Suspended"),

    /** Record is awaiting approval or activation (e.g., new user email verification). */
    PENDING("Pending"),

    /** Record has been soft-deleted. Must not appear in any business queries. */
    DELETED("Deleted");

    /** Human-readable label for UI display. */
    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
