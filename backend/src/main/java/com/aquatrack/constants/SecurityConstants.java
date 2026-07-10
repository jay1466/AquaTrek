package com.aquatrack.constants;

/**
 * Security-related constants for AquaTrack's authentication and authorization layer.
 *
 * <p>These constants define the public URL whitelist, header names,
 * and Spring Security authority strings used across filters and configs.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("SecurityConstants is a utility class.");
    }

    /** Authorization header name. */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Prefix stripped from the Authorization header to extract the raw JWT. */
    public static final String BEARER_PREFIX = "Bearer ";

    /** Spring Security authority for SUPER_ADMIN role. */
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    /** Spring Security authority for ADMIN role. */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** Spring Security authority for MANAGER role. */
    public static final String ROLE_MANAGER = "ROLE_MANAGER";

    /** Spring Security authority for RESIDENT role. */
    public static final String ROLE_RESIDENT = "ROLE_RESIDENT";

    /**
     * URL patterns that are publicly accessible without a JWT token.
     * Any change here must be mirrored in the SecurityConfig permit list.
     */
    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/api-docs/**",
            "/api-docs",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/info",
            "/error"
    };
}
