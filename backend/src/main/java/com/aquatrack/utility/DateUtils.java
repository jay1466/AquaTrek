package com.aquatrack.utility;

import com.aquatrack.constants.AppConstants;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * Utility class for date and time operations in AquaTrack.
 *
 * <p>All date operations use {@link LocalDateTime}/{@link LocalDate} — never
 * {@link java.util.Date} or {@link java.util.Calendar} in new code.
 * Conversions are provided where legacy APIs require {@link Date}.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("DateUtils is a utility class.");
    }

    /** Standard formatters — pre-built for efficiency. */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT);

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(AppConstants.DATETIME_FORMAT);

    private static final DateTimeFormatter MONTH_YEAR_FORMATTER =
            DateTimeFormatter.ofPattern(AppConstants.MONTH_YEAR_FORMAT);

    // =========================================================================
    // Formatting
    // =========================================================================

    /**
     * Formats a {@link LocalDate} to the standard AquaTrack display format (dd-MMM-yyyy).
     *
     * @param date the date to format; returns empty string if null
     * @return formatted date string
     */
    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    /**
     * Formats a {@link LocalDateTime} to the standard display format.
     *
     * @param dateTime the date-time to format; returns empty string if null
     * @return formatted date-time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Formats a {@link YearMonth} for billing cycle labels (e.g., "Jan-2024").
     *
     * @param yearMonth the month/year to format
     * @return formatted month-year string
     */
    public static String formatMonthYear(YearMonth yearMonth) {
        return yearMonth == null ? "" : yearMonth.atDay(1).format(MONTH_YEAR_FORMATTER);
    }

    // =========================================================================
    // Calculation
    // =========================================================================

    /**
     * Calculates the number of days between two dates (inclusive of start, exclusive of end).
     *
     * @param start the start date
     * @param end   the end date
     * @return number of days between start and end
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Returns the first day of a given month.
     *
     * @param yearMonth the target month
     * @return the first day of the month as {@link LocalDate}
     */
    public static LocalDate firstDayOfMonth(YearMonth yearMonth) {
        return yearMonth.atDay(1);
    }

    /**
     * Returns the last day of a given month.
     *
     * @param yearMonth the target month
     * @return the last day of the month as {@link LocalDate}
     */
    public static LocalDate lastDayOfMonth(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth();
    }

    /**
     * Returns the first day of the current month.
     *
     * @return the first day of this month
     */
    public static LocalDate startOfCurrentMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Returns the last day of the current month.
     *
     * @return the last day of this month
     */
    public static LocalDate endOfCurrentMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Checks whether a given date is in the past (before today).
     *
     * @param date the date to check
     * @return true if date is before today
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Checks whether a given date-time is expired (before now).
     *
     * @param dateTime the date-time to check
     * @return true if date-time is before now
     */
    public static boolean isExpired(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Adds a specified number of minutes to a given date-time.
     *
     * @param dateTime the base date-time
     * @param minutes  number of minutes to add
     * @return the resulting date-time
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    /**
     * Adds a specified number of hours to a given date-time.
     *
     * @param dateTime the base date-time
     * @param hours    number of hours to add
     * @return the resulting date-time
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    // =========================================================================
    // Conversion
    // =========================================================================

    /**
     * Converts a {@link LocalDateTime} to a legacy {@link Date} object.
     * Needed for APIs like JWT that still use {@code java.util.Date}.
     *
     * @param localDateTime the date-time to convert
     * @return equivalent {@link Date} in the system default timezone
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Converts a {@link Date} to {@link LocalDateTime} using the system default timezone.
     *
     * @param date the date to convert
     * @return equivalent {@link LocalDateTime}
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Returns the current UTC time.
     *
     * @return current {@link LocalDateTime} in UTC
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Returns the number of months between two YearMonth values.
     *
     * @param start the start month
     * @param end   the end month
     * @return number of months between them
     */
    public static long monthsBetween(YearMonth start, YearMonth end) {
        return ChronoUnit.MONTHS.between(start, end);
    }
}
