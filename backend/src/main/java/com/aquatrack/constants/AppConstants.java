package com.aquatrack.constants;

/**
 * Application-wide constants for AquaTrack.
 *
 * <p>All magic strings, numeric limits, and configuration keys used
 * across the application are centralised here. Never use inline string
 * literals in business logic — always reference this class.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public final class AppConstants {

    /** Prevent instantiation of utility class. */
    private AppConstants() {
        throw new UnsupportedOperationException("AppConstants is a utility class and cannot be instantiated.");
    }

    // =========================================================================
    // Pagination Defaults
    // =========================================================================

    /** Default page number for paginated queries (0-based). */
    public static final int DEFAULT_PAGE_NUMBER = 0;

    /** Default number of records per page. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** Maximum allowed page size to prevent unbounded queries. */
    public static final int MAX_PAGE_SIZE = 100;

    /** Default sort field for most list queries. */
    public static final String DEFAULT_SORT_BY = "createdAt";

    /** Default sort direction. */
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // =========================================================================
    // JWT / Token
    // =========================================================================

    /** Claim key for the apartment (tenant) ID embedded in the JWT. */
    public static final String JWT_CLAIM_APARTMENT_ID = "apartmentId";

    /** Claim key for the user's role embedded in the JWT. */
    public static final String JWT_CLAIM_ROLE = "role";

    /** Claim key for the user's full name in the JWT. */
    public static final String JWT_CLAIM_FULL_NAME = "fullName";

    /** Prefix for Bearer token in the Authorization header. */
    public static final String BEARER_PREFIX = "Bearer ";

    // =========================================================================
    // Security
    // =========================================================================

    /** Maximum failed login attempts before account is locked. */
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    /** Duration (in minutes) for which a locked account remains locked. */
    public static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;

    /** OTP expiry duration in minutes. */
    public static final int OTP_EXPIRY_MINUTES = 15;

    /** Password reset link validity in hours. */
    public static final int PASSWORD_RESET_EXPIRY_HOURS = 24;

    /** Email verification link validity in hours. */
    public static final int EMAIL_VERIFICATION_EXPIRY_HOURS = 48;

    // =========================================================================
    // Billing
    // =========================================================================

    /** Unit of water measurement used in the billing engine. */
    public static final String WATER_UNIT = "KL";   // Kilolitres

    /** Default late fee percentage applied after due date. */
    public static final double DEFAULT_LATE_FEE_PERCENTAGE = 2.0;

    /** Grace period in days after due date before late fee is applied. */
    public static final int LATE_FEE_GRACE_DAYS = 7;

    // =========================================================================
    // Meter Readings
    // =========================================================================

    /** Maximum allowed percentage increase in reading to detect data entry errors. */
    public static final double MAX_READING_INCREASE_PERCENT = 500.0;

    /** Minimum daily consumption (KL) below which a leak alert is triggered. */
    public static final double LEAK_DETECTION_THRESHOLD_KL_PER_DAY = 0.5;

    // =========================================================================
    // Date / Time Formats
    // =========================================================================

    /** Standard date format used in PDF invoices and exports. */
    public static final String DATE_FORMAT = "dd-MMM-yyyy";

    /** Standard date-time format for audit logs and API responses. */
    public static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";

    /** Month-Year format used in billing cycle labels. */
    public static final String MONTH_YEAR_FORMAT = "MMM-yyyy";

    // =========================================================================
    // Invoice
    // =========================================================================

    /** Prefix for auto-generated invoice numbers. */
    public static final String INVOICE_NUMBER_PREFIX = "INV";

    /** Prefix for auto-generated meter serial numbers. */
    public static final String METER_SERIAL_PREFIX = "MTR";

    // =========================================================================
    // System User
    // =========================================================================

    /** Identifier used when an audit action is triggered by a scheduled task. */
    public static final String SYSTEM_USER = "SYSTEM";

    // =========================================================================
    // Cache Names
    // =========================================================================

    public static final String CACHE_TARIFF_PLANS = "tariffPlans";
    public static final String CACHE_SYSTEM_CONFIG = "systemConfig";
    public static final String CACHE_APARTMENTS = "apartments";

    // =========================================================================
    // Shared Area Types
    // =========================================================================

    public static final String SHARED_AREA_GARDEN = "GARDEN";
    public static final String SHARED_AREA_SWIMMING_POOL = "SWIMMING_POOL";
    public static final String SHARED_AREA_PARKING = "PARKING";
    public static final String SHARED_AREA_LOBBY = "LOBBY";
    public static final String SHARED_AREA_SECURITY = "SECURITY_ROOM";
    public static final String SHARED_AREA_CLUBHOUSE = "CLUBHOUSE";
}
