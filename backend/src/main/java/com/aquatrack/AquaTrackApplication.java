package com.aquatrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AquaTrack Application Entry Point.
 *
 * <p>AquaTrack is a multi-tenant, enterprise-grade water consumption and billing
 * management platform designed for apartment societies. This application supports
 * multiple apartment societies on a single deployment with strict tenant isolation.</p>
 *
 * <p>Key capabilities:
 * <ul>
 *   <li>Multi-tenant architecture with apartment-level data isolation</li>
 *   <li>Water meter management and reading collection</li>
 *   <li>Tier-based billing engine with dynamic tariff support</li>
 *   <li>Bulk water purchase tracking and distribution</li>
 *   <li>PDF invoice generation with QR codes</li>
 *   <li>Anomaly detection and alert engine</li>
 *   <li>Analytics and consumption trend reporting</li>
 * </ul>
 * </p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableAsync
@EnableScheduling
@EnableCaching
public class AquaTrackApplication {

    /**
     * Main method — bootstraps the Spring Boot application.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(AquaTrackApplication.class, args);
    }
}
