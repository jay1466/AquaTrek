package com.aquatrack;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Application context smoke test.
 *
 * <p>Verifies that the full Spring Boot application context loads successfully,
 * including all beans, database migrations, and security configuration.
 * Uses Testcontainers to spin up a real PostgreSQL instance — no mocking.</p>
 *
 * @author AquaTrack Engineering Team
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@Testcontainers
class AquaTrackApplicationTests {

    /**
     * Shared PostgreSQL container reused across all tests in this class.
     * Testcontainers automatically starts and stops this container.
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("aquatrack_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    /**
     * Injects the Testcontainer connection properties into Spring's environment
     * before the context is loaded, overriding the defaults in application.yml.
     */
    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Disable scheduled tasks during tests to prevent side effects
        registry.add("spring.task.scheduling.pool.size", () -> 0);
    }

    /**
     * Verifies that the Spring Boot application context starts without errors.
     * If any bean fails to initialize, this test will fail with a descriptive message.
     */
    @Test
    @DisplayName("Should load the Spring application context successfully")
    void contextLoads() {
        // No assertions needed — if Spring context fails to start, the test fails.
        // This catches misconfigured beans, missing properties, and Flyway migration errors.
    }
}
