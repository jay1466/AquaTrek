package com.aquatrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing configuration for AquaTrack.
 *
 * <p>Registers an {@link AuditorAware} bean that supplies the currently
 * authenticated user's email address (or "SYSTEM" for scheduled/background tasks)
 * to JPA's {@code @CreatedBy} and {@code @LastModifiedBy} fields.</p>
 *
 * <p>The {@code @EnableJpaAuditing} annotation is placed on
 * {@link com.aquatrack.AquaTrackApplication} with the explicit
 * {@code auditorAwareRef = "auditorProvider"} so Spring knows which bean to use.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Configuration
public class AuditConfig {

    /** Fallback auditor name used when no authenticated principal is present. */
    private static final String SYSTEM_AUDITOR = "SYSTEM";

    /** The string representation of Spring Security's anonymous user. */
    private static final String ANONYMOUS_USER = "anonymousUser";

    /**
     * Provides the current auditor (user identifier) for JPA audit fields.
     *
     * <p>Resolution logic:
     * <ol>
     *   <li>Fetch the {@link Authentication} from the {@link SecurityContextHolder}.</li>
     *   <li>If authenticated and not anonymous, return the principal name (email).</li>
     *   <li>Otherwise, return "SYSTEM" for background/scheduled operations.</li>
     * </ol>
     * </p>
     *
     * @return an {@link AuditorAware} bean that supplies the current auditor
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Return "SYSTEM" if no authentication context is present
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of(SYSTEM_AUDITOR);
            }

            // Return "SYSTEM" for anonymous (unauthenticated) requests
            Object principal = authentication.getPrincipal();
            if (ANONYMOUS_USER.equals(principal)) {
                return Optional.of(SYSTEM_AUDITOR);
            }

            // Return the authenticated user's name (email in our case)
            return Optional.of(authentication.getName());
        };
    }
}
