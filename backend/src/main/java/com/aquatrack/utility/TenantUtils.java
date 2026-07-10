package com.aquatrack.utility;

import com.aquatrack.constants.AppConstants;
import com.aquatrack.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Utility for extracting the current tenant (apartment) ID from the
 * authenticated JWT principal in any layer of the application.
 *
 * <p>In AquaTrack's multi-tenant architecture, the apartment ID is embedded
 * in the JWT token and extracted by the JWT filter into a custom
 * {@code Authentication} principal. Every service method that accesses
 * tenant-scoped data MUST call {@link #getCurrentApartmentId()} and include
 * it in repository queries to enforce tenant isolation.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public final class TenantUtils {

    private TenantUtils() {
        throw new UnsupportedOperationException("TenantUtils is a utility class.");
    }

    /**
     * Retrieves the apartment ID of the currently authenticated user from
     * the Spring {@link SecurityContextHolder}.
     *
     * <p>The apartment ID is stored as a claim in the JWT and loaded into
     * the {@code Authentication} details map by the JWT authentication filter.</p>
     *
     * @return the current user's apartment UUID
     * @throws UnauthorizedException if no authentication context is present
     *                               or the apartment ID claim is missing
     */
    @SuppressWarnings("unchecked")
    public static UUID getCurrentApartmentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated session found.");
        }

        // The JWT filter populates the details map with JWT claims
        if (authentication.getDetails() instanceof java.util.Map<?, ?> claims) {
            Object apartmentIdObj = claims.get(AppConstants.JWT_CLAIM_APARTMENT_ID);
            if (apartmentIdObj != null) {
                try {
                    return UUID.fromString(apartmentIdObj.toString());
                } catch (IllegalArgumentException e) {
                    throw new UnauthorizedException("Invalid apartment identifier in token.");
                }
            }
        }

        throw new UnauthorizedException(
                "Apartment context is missing from the authentication token. " +
                "Please log in again.");
    }

    /**
     * Retrieves the email/username of the currently authenticated user.
     *
     * @return the authenticated user's email
     * @throws UnauthorizedException if no authenticated session is found
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated session found.");
        }

        return authentication.getName();
    }

    /**
     * Validates that the given resource's apartment ID matches the current
     * tenant's apartment ID. Throws {@link com.aquatrack.exception.TenantAccessException}
     * if there is a mismatch — this prevents cross-tenant data access.
     *
     * @param resourceApartmentId the apartment ID stored on the resource being accessed
     * @throws com.aquatrack.exception.TenantAccessException if IDs do not match
     */
    public static void validateTenantAccess(UUID resourceApartmentId) {
        UUID currentApartmentId = getCurrentApartmentId();
        if (!currentApartmentId.equals(resourceApartmentId)) {
            // Throw generic exception — do not reveal the actual apartment IDs
            throw new com.aquatrack.exception.TenantAccessException();
        }
    }
}
