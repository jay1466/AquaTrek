package com.aquatrack.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

/**
 * General application configuration for AquaTrack.
 *
 * <p>Registers application-wide beans:
 * <ul>
 *   <li>BCrypt password encoder (strength 12)</li>
 *   <li>Customised Jackson ObjectMapper with Java 8 time support</li>
 *   <li>Async thread pool executor for non-blocking operations</li>
 *   <li>RestTemplate for outbound HTTP calls</li>
 * </ul>
 * </p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Configuration
public class AppConfig implements AsyncConfigurer {

    /**
     * BCrypt password encoder with strength factor 12.
     *
     * <p>Strength 12 provides a good balance between security and performance.
     * Higher values increase hashing time (brute-force resistance) but slow
     * down login operations on commodity hardware.</p>
     *
     * @return configured {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Customised Jackson ObjectMapper.
     *
     * <p>Configuration:
     * <ul>
     *   <li>Java 8 date/time types serialized to ISO-8601 strings (not timestamps)</li>
     *   <li>Unknown properties ignored to allow graceful API evolution</li>
     *   <li>Null values omitted from JSON output for lean responses</li>
     * </ul>
     * </p>
     *
     * @return primary {@link ObjectMapper} instance
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());

        // Write dates as ISO-8601 strings, not numeric timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Do not fail on unknown fields — allows backward-compatible API changes
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }

    /**
     * Thread pool executor for {@code @Async} methods.
     *
     * <p>Used for:
     * <ul>
     *   <li>Sending verification and notification emails</li>
     *   <li>PDF invoice generation</li>
     *   <li>Audit log writes</li>
     * </ul>
     * </p>
     *
     * @return configured {@link ThreadPoolTaskExecutor}
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("AquaTrack-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * RestTemplate bean for outbound HTTP calls (e.g., payment gateway, webhooks).
     *
     * @return a default {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
