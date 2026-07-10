package com.aquatrack.enums;

/**
 * Status lifecycle of a generated invoice.
 */
public enum InvoiceStatus {

    /** Invoice is being generated (transient state during PDF creation). */
    GENERATING("Generating"),

    /** Invoice has been generated but not yet sent to the resident. */
    GENERATED("Generated"),

    /** Invoice has been sent to the resident via email. */
    SENT("Sent"),

    /** Invoice has been viewed by the resident. */
    VIEWED("Viewed"),

    /** Invoice payment is due but not yet received. */
    DUE("Due"),

    /** Invoice payment is overdue (past due date). */
    OVERDUE("Overdue"),

    /** Invoice has been fully paid. */
    PAID("Paid"),

    /** Invoice has been partially paid. */
    PARTIALLY_PAID("Partially Paid"),

    /** Invoice was cancelled or voided. */
    CANCELLED("Cancelled"),

    /** Invoice was disputed by the resident. */
    DISPUTED("Disputed");

    private final String displayName;
    InvoiceStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
