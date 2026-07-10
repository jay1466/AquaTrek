package com.aquatrack.enums;

/**
 * Source of water supplied to an apartment society.
 * Used to categorise bulk water purchase records.
 */
public enum WaterSourceType {

    /** Water delivered by tanker trucks. */
    TANKER("Tanker"),

    /** Water supplied by the municipal corporation. */
    MUNICIPAL("Municipal Supply"),

    /** Water from an on-site borewell. */
    BOREWELL("Borewell"),

    /** Recycled or treated grey water. */
    RECYCLED("Recycled Water"),

    /** Water from a rainwater harvesting system. */
    RAINWATER_HARVESTING("Rainwater Harvesting"),

    /** Other or mixed sources. */
    OTHER("Other");

    private final String displayName;
    WaterSourceType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
