package com.aquatrack.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiting filter using Bucket4j's token bucket algorithm.
 *
 * <p>Each unique IP address gets its own token bucket. Requests consume
 * one token per call. When the bucket is empty, subsequent requests are
 * rejected with HTTP 429 Too Many Requests until the bucket refills.</p>
 *
 * <p>Auth endpoints (login, register, forgot-password) are rate-limited
 * more aggressively to mitigate brute-force and credential-stuffing attacks.
 * Other endpoints use the global rate limit configured in application.yml.</p>
 *
 * <p>For production deployments with multiple instances, replace the in-memory
 * {@link ConcurrentHashMap} with a distributed cache (Redis + Bucket4j-Redis).</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.capacity:100}")
    private long capacity;

    @Value("${app.rate-limit.refill-tokens:100}")
    private long refillTokens;

    @Value("${app.rate-limit.refill-period-seconds:60}")
    private long refillPeriodSeconds;

    /** Stricter limits for authentication endpoints to prevent brute-force. */
    private static final long AUTH_CAPACITY       = 10;
    private static final long AUTH_REFILL_TOKENS  = 10;
    private static final long AUTH_REFILL_SECONDS = 60;

    /** In-memory bucket cache — keyed by IP address. */
    private final ConcurrentHashMap<String, Bucket> globalBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> authBuckets   = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = extractClientIp(request);
        String path = request.getServletPath();
        boolean isAuthEndpoint = isAuthEndpoint(path);

        Bucket bucket = isAuthEndpoint
                ? authBuckets.computeIfAbsent(clientIp, ip -> createAuthBucket())
                : globalBuckets.computeIfAbsent(clientIp, ip -> createGlobalBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP [{}] on path [{}]", clientIp, path);
            sendTooManyRequestsResponse(response, clientIp);
        }
    }

    /**
     * Creates a token bucket for the global rate limit.
     */
    private Bucket createGlobalBucket() {
        Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.greedy(refillTokens, Duration.ofSeconds(refillPeriodSeconds))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a more restrictive token bucket for auth endpoints.
     */
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(
                AUTH_CAPACITY,
                Refill.greedy(AUTH_REFILL_TOKENS, Duration.ofSeconds(AUTH_REFILL_SECONDS))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Determines whether the requested path is an authentication endpoint
     * (login, register, forgot-password) that requires stricter rate limiting.
     */
    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/register")
                || path.startsWith("/api/v1/auth/forgot-password");
    }

    /**
     * Extracts the real client IP address, respecting common proxy headers.
     *
     * <p>Order of precedence:
     * X-Forwarded-For → X-Real-IP → RemoteAddr</p>
     *
     * @param request the incoming HTTP request
     * @return the client IP address as a string
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For may contain a chain of IPs; take the first (original client)
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Writes a 429 Too Many Requests JSON response.
     */
    private void sendTooManyRequestsResponse(HttpServletResponse response, String clientIp)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "Too Many Requests");
        body.put("message", "Rate limit exceeded. Please slow down and try again shortly.");
        body.put("timestamp", LocalDateTime.now().toString());

        objectMapper.writeValue(response.getWriter(), body);
    }
}
