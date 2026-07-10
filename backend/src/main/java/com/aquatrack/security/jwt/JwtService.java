package com.aquatrack.security.jwt;

import com.aquatrack.constants.AppConstants;
import com.aquatrack.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service responsible for all JWT token operations in AquaTrack.
 *
 * <p>Tokens contain the following claims:
 * <ul>
 *   <li>{@code sub}         — user email (standard subject claim)</li>
 *   <li>{@code jti}         — unique token ID (used for blacklisting on logout)</li>
 *   <li>{@code apartmentId} — tenant identifier for multi-tenant isolation</li>
 *   <li>{@code role}        — Spring Security authority string (e.g., ROLE_ADMIN)</li>
 *   <li>{@code fullName}    — user's full name (for display in the frontend)</li>
 *   <li>{@code userId}      — user UUID</li>
 * </ul>
 * </p>
 *
 * <p>The secret key is read from {@code app.jwt.secret} and must be
 * at least 256 bits (32 bytes) for HMAC-SHA-256. Any shorter value
 * will cause a {@link io.jsonwebtoken.security.WeakKeyException} at startup.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    // ── Token Generation ──────────────────────────────────────

    /**
     * Generates a JWT access token for the given user.
     *
     * <p>The token is signed with HMAC-SHA-256 using the configured secret.
     * All AquaTrack-specific claims (apartmentId, role, fullName, userId)
     * are embedded so the filter can extract them without a DB round-trip.</p>
     *
     * @param user the authenticated user
     * @return signed JWT access token string
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AppConstants.JWT_CLAIM_APARTMENT_ID,
                user.getApartmentId() != null ? user.getApartmentId().toString() : null);
        claims.put(AppConstants.JWT_CLAIM_ROLE, user.getRole().getName().getSpringSecurityName());
        claims.put(AppConstants.JWT_CLAIM_FULL_NAME, user.getFullName());
        claims.put("userId", user.getId().toString());

        return buildToken(claims, user.getEmail(), accessTokenExpiryMs);
    }

    /**
     * Generates a raw (plain UUID string) refresh token.
     *
     * <p>The raw token is returned to the client and stored in an HTTP-only cookie
     * or secure storage. Only its SHA-256 hash is persisted in the database
     * via {@link com.aquatrack.entity.RefreshToken}.</p>
     *
     * @return a UUID-based raw refresh token string
     */
    public String generateRawRefreshToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    // ── Claim Extraction ──────────────────────────────────────

    /**
     * Extracts the subject (email) from a JWT token.
     *
     * @param token the raw JWT string
     * @return the email embedded in the {@code sub} claim
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the apartment ID from the JWT's custom claim.
     *
     * @param token the raw JWT string
     * @return the apartment UUID, or null for SUPER_ADMIN tokens
     */
    public UUID extractApartmentId(String token) {
        String raw = extractClaim(token, claims ->
                claims.get(AppConstants.JWT_CLAIM_APARTMENT_ID, String.class));
        return raw != null ? UUID.fromString(raw) : null;
    }

    /**
     * Extracts the role authority string (e.g., "ROLE_ADMIN") from the JWT.
     *
     * @param token the raw JWT string
     * @return the Spring Security authority string
     */
    public String extractRole(String token) {
        return extractClaim(token, claims ->
                claims.get(AppConstants.JWT_CLAIM_ROLE, String.class));
    }

    /**
     * Extracts the unique JWT ID ({@code jti} claim) used for token blacklisting.
     *
     * @param token the raw JWT string
     * @return the token's unique identifier
     */
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * Extracts the user UUID from the JWT's custom claim.
     *
     * @param token the raw JWT string
     * @return the user UUID
     */
    public UUID extractUserId(String token) {
        String raw = extractClaim(token, claims -> claims.get("userId", String.class));
        return raw != null ? UUID.fromString(raw) : null;
    }

    /**
     * Returns the expiry timestamp of the token as a {@link LocalDateTime}.
     *
     * @param token the raw JWT string
     * @return expiry date-time in the system default timezone
     */
    public LocalDateTime extractExpiry(String token) {
        Date expiry = extractClaim(token, Claims::getExpiration);
        return expiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // ── Validation ────────────────────────────────────────────

    /**
     * Validates a JWT token against the expected username.
     *
     * <p>A token is valid if:
     * <ol>
     *   <li>It can be parsed (signature is valid, not tampered)</li>
     *   <li>The {@code sub} claim matches the provided username</li>
     *   <li>The token has not expired</li>
     * </ol>
     * </p>
     *
     * <p>Blacklist checking is intentionally NOT done here — it is
     * done in {@link com.aquatrack.security.filter.JwtAuthFilter}
     * where the full security context is available.</p>
     *
     * @param token    the raw JWT string
     * @param username the expected email/username
     * @return true if the token is valid for this user
     */
    public boolean isTokenValid(String token, String username) {
        try {
            String extractedUsername = extractEmail(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Attempts to parse the token and returns true if parsing succeeds.
     * Does not check blacklist or username — use {@link #isTokenValid(String, String)} for full validation.
     *
     * @param token the raw JWT string to verify
     * @return true if the signature is valid and the token has not expired
     */
    public boolean isTokenStructurallyValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Private Helpers ───────────────────────────────────────

    /**
     * Builds and signs a JWT with the given claims and expiry.
     *
     * @param extraClaims additional claims to embed
     * @param subject     the email/subject claim
     * @param expiryMs    token lifetime in milliseconds
     * @return the signed JWT string
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, long expiryMs) {
        long nowMs = System.currentTimeMillis();

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .id(UUID.randomUUID().toString())      // jti — unique per token
                .issuedAt(new Date(nowMs))
                .expiration(new Date(nowMs + expiryMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts a single claim from the token using the provided resolver function.
     *
     * @param token          the raw JWT string
     * @param claimsResolver function to extract the desired claim
     * @param <T>            the claim value type
     * @return the extracted claim value
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(parseToken(token).getPayload());
    }

    /**
     * Parses and verifies the JWT, returning the Jws (signed claims) wrapper.
     *
     * @param token the raw JWT string
     * @return parsed and verified Jws object
     * @throws JwtException if the token is invalid, expired, or tampered
     */
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }

    /**
     * Returns the HMAC-SHA-256 signing key derived from the configured secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secretKey.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Checks whether the token's expiry date is in the past.
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
