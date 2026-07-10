package com.aquatrack.enums;

/**
 * Status of a payment transaction against an invoice.
 */
public enum PaymentStatus {

    /** Payment is expected but not yet received. */
    PENDING("Pending"),

    /** Payment has been received and confirmed. */
    PAID("Paid"),

    /** Payment was partially received (e.g. advance payment). */
    PARTIALLY_PAID("Partially Paid"),

    /** Payment due date has passed with no payment received. */
    OVERDUE("Overdue"),

    /** Payment was refunded to the resident. */
    REFUNDED("Refunded"),

    /** Payment failed (e.g. bounced cheque, failed online transaction). */
    FAILED("Failed"),

    /** Invoice was waived off by the administrator. */
    WAIVED("Waived");

    private final String displayName;
    PaymentStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
