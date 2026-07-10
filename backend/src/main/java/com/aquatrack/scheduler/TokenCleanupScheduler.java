package com.aquatrack.scheduler;

import com.aquatrack.repository.RefreshTokenRepository;
import com.aquatrack.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled job to clean up expired token records from the database.
 *
 * <p>Token tables grow continuously during normal operation. This scheduler
 * removes records that are no longer needed for any security check:
 * <ul>
 *   <li>Expired access token blacklist entries (they can't be used anyway)</li>
 *   <li>Expired and revoked refresh tokens</li>
 * </ul>
 * </p>
 *
 * <p>Runs nightly at 02:00 AM server time to minimise load during peak hours.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final TokenBlacklistRepository  tokenBlacklistRepository;
    private final RefreshTokenRepository    refreshTokenRepository;

    /**
     * Removes expired token blacklist entries.
     * Entries whose {@code expires_at} is in the past are safe to delete —
     * the JWT itself is already expired and cannot be presented.
     *
     * Scheduled: 02:00 AM daily.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredBlacklistEntries() {
        log.info("Starting expired token blacklist cleanup...");
        try {
            int deleted = tokenBlacklistRepository.deleteExpiredEntries(LocalDateTime.now());
            log.info("Token blacklist cleanup complete — {} expired entries removed.", deleted);
        } catch (Exception e) {
            log.error("Token blacklist cleanup failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Removes expired and revoked refresh tokens.
     * Runs 10 minutes after the blacklist cleanup to spread DB load.
     *
     * Scheduled: 02:10 AM daily.
     */
    @Scheduled(cron = "0 10 2 * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        log.info("Starting expired refresh token cleanup...");
        try {
            // Tokens expired more than 1 day ago are safe to remove
            LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
            int deleted = refreshTokenRepository.deleteExpiredAndRevoked(cutoff);
            log.info("Refresh token cleanup complete — {} expired/revoked tokens removed.", deleted);
        } catch (Exception e) {
            log.error("Refresh token cleanup failed: {}", e.getMessage(), e);
        }
    }
}
