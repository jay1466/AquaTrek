package com.aquatrack.security.filter;

import com.aquatrack.constants.AppConstants;
import com.aquatrack.constants.SecurityConstants;
import com.aquatrack.repository.TokenBlacklistRepository;
import com.aquatrack.security.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Authentication Filter for AquaTrack.
 *
 * <p>Executed once per request, this filter:
 * <ol>
 *   <li>Skips public endpoints (no Authorization header required)</li>
 *   <li>Extracts the Bearer token from the Authorization header</li>
 *   <li>Validates the token signature and expiry via {@link JwtService}</li>
 *   <li>Checks the token against the blacklist (for logged-out tokens)</li>
 *   <li>Loads the user's details and populates the {@link SecurityContextHolder}</li>
 *   <li>Populates {@code authentication.details} with JWT claims (including apartmentId)</li>
 * </ol>
 * </p>
 *
 * <p>The {@code apartmentId} is stored in {@code authentication.details} as a map,
 * allowing {@link com.aquatrack.utility.TenantUtils#getCurrentApartmentId()} to
 * retrieve it without a database call.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final ObjectMapper objectMapper;

    /**
     * Core filter logic — executed on every HTTP request.
     *
     * <p>If token validation fails for any reason, the request is allowed to
     * continue without authentication. Spring Security will then reject it
     * at the endpoint level based on security configuration.</p>
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ── Step 1: Extract Authorization header ──────────────────
        final String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);

        // If no Bearer token, continue chain — Spring Security will handle auth
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

        try {
            // ── Step 2: Validate token structure ──────────────────
            if (!jwtService.isTokenStructurallyValid(token)) {
                log.debug("Invalid JWT token structure for request: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // ── Step 3: Check token blacklist ─────────────────────
            String jti = jwtService.extractJti(token);
            if (tokenBlacklistRepository.existsByTokenJti(jti)) {
                log.warn("Blacklisted token used for request: {}", request.getRequestURI());
                sendUnauthorizedResponse(response, "Token has been revoked. Please log in again.");
                return;
            }

            // ── Step 4: Extract email and validate ────────────────
            String email = jwtService.extractEmail(token);

            // Only authenticate if not already authenticated in this request
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                var userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails.getUsername())) {

                    // ── Step 5: Build authentication token ────────
                    String roleAuthority = jwtService.extractRole(token);

                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            List.of(new SimpleGrantedAuthority(roleAuthority))
                    );

                    // ── Step 6: Embed JWT claims in details map ────
                    // This is how TenantUtils.getCurrentApartmentId() accesses the tenant ID
                    // without a DB round-trip on every request.
                    Map<String, Object> claimsMap = new HashMap<>();
                    UUID apartmentId = jwtService.extractApartmentId(token);
                    claimsMap.put(AppConstants.JWT_CLAIM_APARTMENT_ID,
                            apartmentId != null ? apartmentId.toString() : null);
                    claimsMap.put(AppConstants.JWT_CLAIM_ROLE, roleAuthority);
                    claimsMap.put("jti", jti);

                    authToken.setDetails(claimsMap);

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated user [{}] for request: {}",
                            email, request.getRequestURI());
                }
            }

        } catch (Exception e) {
            log.debug("JWT authentication failed for {}: {}", request.getRequestURI(), e.getMessage());
            // Clear security context on error — do NOT send error response here;
            // let Spring Security handle the 401/403 based on endpoint configuration.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Sends a JSON 401 Unauthorized response directly, bypassing the filter chain.
     * Used specifically when a blacklisted token is detected.
     *
     * @param response the HTTP response
     * @param message  the error message to send
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());

        objectMapper.writeValue(response.getWriter(), body);
    }

    /**
     * Skip this filter for public endpoints to avoid unnecessary processing.
     * The URL patterns here must match those in SecurityConfig's permit list.
     *
     * @param request the incoming request
     * @return true if the filter should not run for this request
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.equals("/actuator/health")
                || path.equals("/actuator/info")
                || path.equals("/error");
    }
}
