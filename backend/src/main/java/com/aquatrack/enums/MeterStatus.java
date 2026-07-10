package com.aquatrack.enums;

/**
 * Operational status of a water meter in the AquaTrack system.
 *
 * <p>The status drives which operations are permitted on the meter
 * and whether readings can be accepted for billing.</p>
 */
public enum MeterStatus {

    /** Meter is installed, calibrated, and actively measuring consumption. */
    ACTIVE("Active"),

    /** Meter has been physically installed but not yet verified/calibrated. */
    INSTALLED("Installed"),

    /** Meter is offline for maintenance but will return to service. */
    UNDER_MAINTENANCE("Under Maintenance"),

    /** Meter has been flagged as faulty and readings may be inaccurate. */
    FAULTY("Faulty"),

    /** Meter has been replaced by a new device; historical readings retained. */
    REPLACED("Replaced"),

    /** Meter has been permanently decommissioned. */
    DECOMMISSIONED("Decommissioned"),

    /** Meter is temporarily disabled (e.g. unit is vacant). */
    INACTIVE("Inactive");

    private final String displayName;

    MeterStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
