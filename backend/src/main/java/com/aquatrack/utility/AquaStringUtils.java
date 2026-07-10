package com.aquatrack.utility;

import com.aquatrack.constants.AppConstants;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * String manipulation and generation utilities for AquaTrack.
 *
 * <p>Wraps Apache Commons {@link StringUtils} with domain-specific helpers
 * for generating invoice numbers, OTPs, slugs, and masked strings.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public final class AquaStringUtils {

    private AquaStringUtils() {
        throw new UnsupportedOperationException("AquaStringUtils is a utility class.");
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // =========================================================================
    // Invoice / Serial Number Generation
    // =========================================================================

    /**
     * Generates a unique invoice number in the format {@code INV-YYYYMM-XXXXXX}.
     *
     * <p>Example: {@code INV-202401-A3F9K2}</p>
     *
     * @return a unique invoice number
     */
    public static String generateInvoiceNumber() {
        String monthYear = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMM"));
        String suffix = randomAlphanumeric(6);
        return AppConstants.INVOICE_NUMBER_PREFIX + "-" + monthYear + "-" + suffix;
    }

    /**
     * Generates a meter serial number in the format {@code MTR-XXXXXXXX}.
     *
     * <p>Example: {@code MTR-A3F9K2B7}</p>
     *
     * @return a unique meter serial number
     */
    public static String generateMeterSerial() {
        return AppConstants.METER_SERIAL_PREFIX + "-" + randomAlphanumeric(8);
    }

    /**
     * Generates a numeric OTP of the specified length.
     *
     * @param length the number of digits in the OTP (typically 6)
     * @return a numeric OTP string with leading zeros preserved
     */
    public static String generateOtp(int length) {
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(DIGITS.charAt(SECURE_RANDOM.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }

    /**
     * Generates a cryptographically secure random token suitable for
     * email verification and password reset links.
     *
     * @return a 128-character hex token
     */
    public static String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    // =========================================================================
    // Masking / Privacy
    // =========================================================================

    /**
     * Masks an email address for display (e.g., {@code jo**@example.com}).
     *
     * @param email the email to mask
     * @return masked email, or the original if it cannot be parsed
     */
    public static String maskEmail(String email) {
        if (StringUtils.isBlank(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return localPart + "**" + domain;
        }
        return localPart.substring(0, 2) + "**" + domain;
    }

    /**
     * Masks a mobile phone number, showing only the last 4 digits.
     *
     * <p>Example: {@code xxxxxxx9876}</p>
     *
     * @param phone the phone number to mask
     * @return masked phone number
     */
    public static String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 4) {
            return "****";
        }
        String visible = phone.substring(phone.length() - 4);
        return "x".repeat(phone.length() - 4) + visible;
    }

    // =========================================================================
    // Slug / URL
    // =========================================================================

    /**
     * Converts an arbitrary string to a URL-safe slug.
     *
     * <p>Example: {@code "Green Valley Apartment"} → {@code "green-valley-apartment"}</p>
     *
     * @param input the input string
     * @return a lowercase, hyphen-separated slug
     */
    public static String toSlug(String input) {
        if (StringUtils.isBlank(input)) return "";

        // Normalize unicode characters to their ASCII equivalents
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        return normalized
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")     // remove special chars
                .replaceAll("[\\s-]+", "-")            // spaces and hyphens to single hyphen
                .replaceAll("^-+|-+$", "");            // strip leading/trailing hyphens
    }

    // =========================================================================
    // Formatting
    // =========================================================================

    /**
     * Capitalises the first letter of each word (title case).
     *
     * @param input the string to convert
     * @return title-cased string
     */
    public static String toTitleCase(String input) {
        if (StringUtils.isBlank(input)) return input;
        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Truncates a string to a maximum length, appending "..." if truncated.
     *
     * @param input     the string to truncate
     * @param maxLength the maximum allowed length (including ellipsis)
     * @return truncated string
     */
    public static String truncate(String input, int maxLength) {
        if (StringUtils.isBlank(input) || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength - 3) + "...";
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    /**
     * Generates a random uppercase alphanumeric string of the given length.
     *
     * @param length the desired string length
     * @return random alphanumeric string
     */
    private static String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
