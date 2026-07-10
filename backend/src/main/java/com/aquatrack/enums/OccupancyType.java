package com.aquatrack.enums;

/**
 * Occupancy category for a household unit.
 * Used in the consumption distribution fallback algorithm.
 */
public enum OccupancyType {

    /** Unit is occupied by the owner. */
    OWNER_OCCUPIED("Owner Occupied"),

    /** Unit is occupied by a tenant. */
    TENANT_OCCUPIED("Tenant Occupied"),

    /** Unit is currently vacant. */
    VACANT("Vacant"),

    /** Unit is under renovation. */
    UNDER_RENOVATION("Under Renovation"),

    /** Unit is used for commercial purposes. */
    COMMERCIAL("Commercial");

    private final String displayName;
    OccupancyType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
