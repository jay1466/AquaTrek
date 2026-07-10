package com.aquatrack.controller;

import com.aquatrack.dto.request.LoginRequest;
import com.aquatrack.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AuthController} using a real PostgreSQL instance
 * via Testcontainers and the full Spring Boot context.
 *
 * <p>These tests validate the complete auth flow:
 * HTTP request → filter → controller → service → repository → DB</p>
 *
 * @author AquaTrack Engineering Team
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Testcontainers
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("aquatrack_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.jwt.secret",
                () -> "VGVzdFNlY3JldEtleUZvckFxdWFUcmFja1Rlc3RpbmdPbmx5TWluMjU2Qml0czIwMjQ=");
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
        registry.add("spring.mail.properties.mail.smtp.auth",            () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
    }

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;

    // ── Registration ──────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/auth/register — valid payload → 201 Created")
    void register_withValidPayload_returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ananya")
                .lastName("Patel")
                .email("ananya.patel.test@example.com")
                .password("Secure@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("verification")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — blank email → 400 Bad Request with validation error")
    void register_withBlankEmail_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email("")
                .password("Secure@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — weak password → 400 with password validation error")
    void register_withWeakPassword_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email("test.weak.pass@example.com")
                .password("password")   // no uppercase, no digit, no special char
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — unknown email → 401 Unauthorized")
    void login_withUnknownEmail_returns401() throws Exception {
        LoginRequest request = new LoginRequest("nobody@nowhere.com", "Secure@123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — missing password field → 400 Bad Request")
    void login_withMissingPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password — any email → 200 OK (no enumeration)")
    void forgotPassword_anyEmail_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"doesnotexist@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(
                        org.hamcrest.Matchers.containsString("If an account with that email")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout — no token → 401 Unauthorized")
    void logout_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }
}
