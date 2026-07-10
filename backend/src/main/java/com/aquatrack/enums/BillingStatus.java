package com.aquatrack.enums;

/**
 * Status of a billing cycle run.
 *
 * <p>A billing cycle progresses: PENDING → PROCESSING → COMPLETED (or FAILED).
 * PUBLISHED indicates invoices have been sent to residents.</p>
 */
public enum BillingStatus {

    /** Billing cycle is scheduled but not yet started. */
    PENDING("Pending"),

    /** Billing engine is currently processing readings and computing charges. */
    PROCESSING("Processing"),

    /** All invoices generated successfully. */
    COMPLETED("Completed"),

    /** Invoices have been published and emailed to residents. */
    PUBLISHED("Published"),

    /** Billing run encountered errors and was not completed. */
    FAILED("Failed"),

    /** Billing cycle was cancelled before completion. */
    CANCELLED("Cancelled");

    private final String displayName;
    BillingStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
