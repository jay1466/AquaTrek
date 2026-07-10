package com.aquatrack.constants;

/**
 * REST API URL path constants for AquaTrack.
 *
 * <p>Centralising URL path segments prevents typos and makes global
 * URL refactoring a single-file change.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException("ApiConstants is a utility class.");
    }

    /** Global API version prefix. */
    public static final String API_V1 = "/api/v1";

    // =========================================================================
    // Auth
    // =========================================================================
    public static final String AUTH_BASE = API_V1 + "/auth";
    public static final String AUTH_REGISTER = "/register";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_REFRESH = "/refresh";
    public static final String AUTH_LOGOUT = "/logout";
    public static final String AUTH_FORGOT_PASSWORD = "/forgot-password";
    public static final String AUTH_RESET_PASSWORD = "/reset-password";
    public static final String AUTH_VERIFY_EMAIL = "/verify-email";
    public static final String AUTH_RESEND_VERIFICATION = "/resend-verification";

    // =========================================================================
    // Apartments
    // =========================================================================
    public static final String APARTMENTS_BASE = API_V1 + "/apartments";

    // =========================================================================
    // Buildings
    // =========================================================================
    public static final String BUILDINGS_BASE = API_V1 + "/buildings";

    // =========================================================================
    // Households
    // =========================================================================
    public static final String HOUSEHOLDS_BASE = API_V1 + "/households";

    // =========================================================================
    // Meters
    // =========================================================================
    public static final String METERS_BASE = API_V1 + "/meters";
    public static final String METER_READINGS_BASE = API_V1 + "/meter-readings";

    // =========================================================================
    // Billing
    // =========================================================================
    public static final String BILLING_BASE = API_V1 + "/billing";
    public static final String TARIFF_BASE = API_V1 + "/tariffs";

    // =========================================================================
    // Invoices
    // =========================================================================
    public static final String INVOICES_BASE = API_V1 + "/invoices";

    // =========================================================================
    // Payments
    // =========================================================================
    public static final String PAYMENTS_BASE = API_V1 + "/payments";

    // =========================================================================
    // Bulk Water
    // =========================================================================
    public static final String BULK_WATER_BASE = API_V1 + "/bulk-water";

    // =========================================================================
    // Alerts
    // =========================================================================
    public static final String ALERTS_BASE = API_V1 + "/alerts";

    // =========================================================================
    // Notifications
    // =========================================================================
    public static final String NOTIFICATIONS_BASE = API_V1 + "/notifications";

    // =========================================================================
    // Analytics
    // =========================================================================
    public static final String ANALYTICS_BASE = API_V1 + "/analytics";
    public static final String DASHBOARD_BASE = API_V1 + "/dashboard";

    // =========================================================================
    // Users
    // =========================================================================
    public static final String USERS_BASE = API_V1 + "/users";

    // =========================================================================
    // Settings
    // =========================================================================
    public static final String SETTINGS_BASE = API_V1 + "/settings";
}
