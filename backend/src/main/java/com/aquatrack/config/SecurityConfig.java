package com.aquatrack.config;

import com.aquatrack.constants.SecurityConstants;
import com.aquatrack.security.filter.JwtAuthFilter;
import com.aquatrack.security.filter.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 6 configuration for AquaTrack.
 *
 * <p>Security model:
 * <ul>
 *   <li>Stateless JWT — no sessions, no cookies from the server side</li>
 *   <li>CSRF disabled (not needed for stateless REST APIs)</li>
 *   <li>All requests to {@code /api/v1/auth/**} are public</li>
 *   <li>All other requests require a valid JWT Bearer token</li>
 *   <li>Method-level security enabled via {@code @EnableMethodSecurity}</li>
 *   <li>CORS configured from application properties</li>
 *   <li>Rate limiting applied before JWT filter</li>
 * </ul>
 * </p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    /**
     * Main security filter chain configuration.
     *
     * <p>Filter execution order (inner to outer):
     * RateLimitFilter → JwtAuthFilter → UsernamePasswordAuthenticationFilter</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // ── Disable CSRF (stateless REST — no form submissions) ──
                .csrf(AbstractHttpConfigurer::disable)

                // ── CORS configuration ────────────────────────────────────
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ── Session management: stateless ─────────────────────────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── Authorization rules ───────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // Public: auth endpoints
                        .requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll()

                        // Public: OPTIONS pre-flight for CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // SUPER_ADMIN only endpoints
                        .requestMatchers("/api/v1/admin/**").hasAuthority(SecurityConstants.ROLE_SUPER_ADMIN)

                        // ADMIN and above
                        .requestMatchers("/api/v1/apartments/**").hasAnyAuthority(
                                SecurityConstants.ROLE_SUPER_ADMIN, SecurityConstants.ROLE_ADMIN)

                        // All remaining requests require any authenticated user
                        .anyRequest().authenticated()
                )

                // ── Custom filters ────────────────────────────────────────
                // Rate limiter runs before JWT filter to block excessive requests early
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                // JWT filter runs before Spring Security's own auth filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // ── Authentication provider ───────────────────────────────
                .authenticationProvider(authenticationProvider())

                .build();
    }

    /**
     * DAO Authentication Provider — uses our {@link UserDetailsService} and BCrypt encoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Exposes Spring's {@link AuthenticationManager} as a bean.
     * Used by {@link com.aquatrack.service.impl.AuthServiceImpl} to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS configuration sourced from application.yml.
     *
     * <p>Allowed origins and methods are configured per environment
     * (dev allows localhost, prod restricts to the actual domain).</p>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Parse comma-separated origins from config
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins.stream().map(String::trim).toList());

        // Parse comma-separated methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        config.setAllowedMethods(methods.stream().map(String::trim).toList());

        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);

        // Expose the Authorization header so the frontend can read it
        config.setExposedHeaders(List.of("Authorization", "X-Request-ID"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
