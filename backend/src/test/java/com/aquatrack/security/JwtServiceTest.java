package com.aquatrack.security;

import com.aquatrack.entity.Role;
import com.aquatrack.entity.User;
import com.aquatrack.enums.UserRole;
import com.aquatrack.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtService}.
 * Tests token generation, claim extraction, and validation logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET =
            "VGVzdFNlY3JldEtleUZvckFxdWFUcmFja1Rlc3RpbmdPbmx5TWluMjU2Qml0czIwMjQ=";

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",            TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs",  900_000L);

        Role role = Role.builder()
                .name(UserRole.ADMIN)
                .displayName("Administrator")
                .build();

        UUID apartmentId = UUID.randomUUID();
        testUser = User.builder()
                .role(role)
                .email("admin@aquatrack.com")
                .firstName("Admin")
                .lastName("User")
                .emailVerified(true)
                .accountLocked(false)
                .apartmentId(apartmentId)
                .build();
        ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("Should generate a non-null access token")
    void generateAccessToken_shouldReturnNonNullToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Generated token should contain three JWT parts (header.payload.signature)")
    void generateAccessToken_shouldHaveThreeParts() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract email correctly from generated token")
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@aquatrack.com");
    }

    @Test
    @DisplayName("Should extract apartment ID correctly from generated token")
    void extractApartmentId_shouldReturnCorrectApartmentId() {
        String token = jwtService.generateAccessToken(testUser);
        UUID extractedId = jwtService.extractApartmentId(token);
        assertThat(extractedId).isEqualTo(testUser.getApartmentId());
    }

    @Test
    @DisplayName("Should extract role correctly from generated token")
    void extractRole_shouldReturnCorrectRole() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should extract unique JTI from generated token")
    void extractJti_shouldReturnNonNullJti() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.extractJti(token)).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Should return true for isTokenValid when token matches the user")
    void isTokenValid_withMatchingEmail_shouldReturnTrue() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.isTokenValid(token, "admin@aquatrack.com")).isTrue();
    }

    @Test
    @DisplayName("Should return false for isTokenValid when email does not match")
    void isTokenValid_withWrongEmail_shouldReturnFalse() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.isTokenValid(token, "other@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should return true for structurally valid token")
    void isTokenStructurallyValid_withValidToken_shouldReturnTrue() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.isTokenStructurallyValid(token)).isTrue();
    }

    @Test
    @DisplayName("Should return false for a garbage token string")
    void isTokenStructurallyValid_withGarbageToken_shouldReturnFalse() {
        assertThat(jwtService.isTokenStructurallyValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("Should return false for an empty string")
    void isTokenStructurallyValid_withEmptyString_shouldReturnFalse() {
        assertThat(jwtService.isTokenStructurallyValid("")).isFalse();
    }

    @Test
    @DisplayName("Each generated token should have a unique JTI")
    void generateAccessToken_eachTokenShouldHaveUniqueJti() {
        String token1 = jwtService.generateAccessToken(testUser);
        String token2 = jwtService.generateAccessToken(testUser);

        String jti1 = jwtService.extractJti(token1);
        String jti2 = jwtService.extractJti(token2);

        assertThat(jti1).isNotEqualTo(jti2);
    }

    @Test
    @DisplayName("generateRawRefreshToken should return non-null, non-blank string")
    void generateRawRefreshToken_shouldReturnNonBlankString() {
        String token = jwtService.generateRawRefreshToken();
        assertThat(token).isNotNull().isNotBlank().contains("-");
    }
}
