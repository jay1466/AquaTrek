package com.aquatrack.enums;

/**
 * Classifies how a meter reading was captured.
 * Used for audit trails and reading quality analysis.
 */
public enum ReadingType {

    /** Reading entered manually by a meter reader or administrator. */
    MANUAL("Manual"),

    /** Reading uploaded via a CSV bulk import file. */
    CSV_UPLOAD("CSV Upload"),

    /** Reading captured automatically by a smart IoT meter device. */
    AUTOMATED("Automated"),

    /** Reading estimated when actual reading was unavailable. */
    ESTIMATED("Estimated"),

    /** Reading entered by the resident via the self-service portal. */
    SELF_REPORTED("Self Reported");

    private final String displayName;
    ReadingType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
