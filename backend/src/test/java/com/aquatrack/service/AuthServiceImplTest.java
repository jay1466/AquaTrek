package com.aquatrack.service;

import com.aquatrack.dto.request.LoginRequest;
import com.aquatrack.dto.request.RegisterRequest;
import com.aquatrack.dto.response.AuthResponse;
import com.aquatrack.entity.Role;
import com.aquatrack.entity.User;
import com.aquatrack.enums.Status;
import com.aquatrack.enums.UserRole;
import com.aquatrack.exception.BadRequestException;
import com.aquatrack.exception.DuplicateResourceException;
import com.aquatrack.exception.UnauthorizedException;
import com.aquatrack.mapper.UserMapper;
import com.aquatrack.repository.*;
import com.aquatrack.security.jwt.JwtService;
import com.aquatrack.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 *
 * <p>All dependencies are mocked with Mockito. No Spring context is loaded,
 * keeping these tests fast (< 1 second total).</p>
 *
 * @author AquaTrack Engineering Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock private UserRepository                   userRepository;
    @Mock private RoleRepository                   roleRepository;
    @Mock private RefreshTokenRepository           refreshTokenRepository;
    @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock private PasswordResetTokenRepository     passwordResetTokenRepository;
    @Mock private TokenBlacklistRepository         tokenBlacklistRepository;
    @Mock private UserLoginAttemptRepository       userLoginAttemptRepository;
    @Mock private JwtService                       jwtService;
    @Mock private AuthenticationManager            authenticationManager;
    @Mock private PasswordEncoder                  passwordEncoder;
    @Mock private EmailService                     emailService;
    @Mock private UserMapper                       userMapper;
    @Mock private HttpServletRequest               httpRequest;

    @InjectMocks
    private AuthServiceImpl authService;

    // ── Test Data ─────────────────────────────────────────────

    private Role adminRole;
    private Role residentRole;
    private User activeUser;
    private UUID apartmentId;

    @BeforeEach
    void setUp() {
        // Inject @Value fields manually (no Spring context)
        ReflectionTestUtils.setField(authService, "accessTokenExpiryMs",  900_000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiryMs", 604_800_000L);

        apartmentId = UUID.randomUUID();

        adminRole = Role.builder()
                .name(UserRole.ADMIN)
                .displayName("Administrator")
                .build();

        residentRole = Role.builder()
                .name(UserRole.RESIDENT)
                .displayName("Resident")
                .build();

        activeUser = User.builder()
                .apartmentId(apartmentId)
                .role(residentRole)
                .email("priya@example.com")
                .passwordHash("$2a$12$hashedPassword")
                .firstName("Priya")
                .lastName("Sharma")
                .emailVerified(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();
        activeUser.setStatus(Status.ACTIVE);
        ReflectionTestUtils.setField(activeUser, "id", UUID.randomUUID());
    }

    // ── Registration Tests ────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new user successfully and return confirmation message")
        void register_withValidRequest_shouldSucceed() {
            RegisterRequest request = RegisterRequest.builder()
                    .apartmentId(apartmentId)
                    .firstName("Priya")
                    .lastName("Sharma")
                    .email("priya@example.com")
                    .password("Secure@123")
                    .build();

            when(userRepository.existsByEmailAndIsDeletedFalse(anyString())).thenReturn(false);
            when(roleRepository.findByNameAndIsDeletedFalse(UserRole.RESIDENT))
                    .thenReturn(Optional.of(residentRole));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashed");
            when(userRepository.save(any(User.class))).thenReturn(activeUser);
            doNothing().when(emailVerificationTokenRepository)
                    .invalidatePreviousTokensForUser(any(UUID.class));
            when(emailVerificationTokenRepository.save(any())).thenReturn(null);
            doNothing().when(emailService).sendVerificationEmail(any(), anyString());
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            String result = authService.register(request, httpRequest);

            assertThat(result).contains("Registration successful");
            verify(userRepository).save(any(User.class));
            verify(emailService).sendVerificationEmail(any(User.class), anyString());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void register_withDuplicateEmail_shouldThrowDuplicateResourceException() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("priya@example.com")
                    .password("Secure@123")
                    .firstName("Priya")
                    .lastName("Sharma")
                    .build();

            when(userRepository.existsByEmailAndIsDeletedFalse("priya@example.com"))
                    .thenReturn(true);
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            assertThatThrownBy(() -> authService.register(request, httpRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("User already exists");
        }

        @Test
        @DisplayName("Should reject SUPER_ADMIN role assignment via API")
        void register_withSuperAdminRole_shouldThrowBadRequestException() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("admin@example.com")
                    .password("Secure@123")
                    .firstName("Admin")
                    .lastName("User")
                    .role(UserRole.SUPER_ADMIN)
                    .build();

            when(userRepository.existsByEmailAndIsDeletedFalse(anyString())).thenReturn(false);
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            assertThatThrownBy(() -> authService.register(request, httpRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("SUPER_ADMIN role cannot be assigned");
        }
    }

    // ── Login Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Should return AuthResponse with tokens on successful login")
        void login_withValidCredentials_shouldReturnAuthResponse() {
            LoginRequest request = new LoginRequest("priya@example.com", "Secure@123");

            when(userRepository.findByEmailAndIsDeletedFalse("priya@example.com"))
                    .thenReturn(Optional.of(activeUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            doNothing().when(userRepository).recordSuccessfulLogin(any(), any(), anyString());
            when(userRepository.findByEmailAndIsDeletedFalse("priya@example.com"))
                    .thenReturn(Optional.of(activeUser));
            when(jwtService.generateAccessToken(any(User.class))).thenReturn("mock.access.token");
            when(jwtService.generateRawRefreshToken()).thenReturn("mock-refresh-token");
            when(refreshTokenRepository.save(any())).thenReturn(null);
            when(userMapper.toUserSummary(any(User.class)))
                    .thenReturn(AuthResponse.UserSummary.builder()
                            .id(activeUser.getId())
                            .email("priya@example.com")
                            .fullName("Priya Sharma")
                            .role(UserRole.RESIDENT)
                            .build());
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
            when(httpRequest.getHeader("User-Agent")).thenReturn("TestAgent/1.0");
            when(userLoginAttemptRepository.save(any())).thenReturn(null);

            AuthResponse response = authService.login(request, httpRequest);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("mock.access.token");
            assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
            assertThat(response.getUser().getEmail()).isEqualTo("priya@example.com");
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when password is wrong")
        void login_withWrongPassword_shouldThrowBadCredentials() {
            LoginRequest request = new LoginRequest("priya@example.com", "WrongPass");

            when(userRepository.findByEmailAndIsDeletedFalse("priya@example.com"))
                    .thenReturn(Optional.of(activeUser));
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
            when(httpRequest.getHeader("User-Agent")).thenReturn("TestAgent");
            when(userLoginAttemptRepository.save(any())).thenReturn(null);
            doNothing().when(userRepository).updateLoginLockStatus(any(), anyInt(), anyBoolean(), any());

            assertThatThrownBy(() -> authService.login(request, httpRequest))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when email is not verified")
        void login_withUnverifiedEmail_shouldThrowUnauthorizedException() {
            User unverifiedUser = User.builder()
                    .email("priya@example.com")
                    .passwordHash("$2a$12$hash")
                    .firstName("Priya")
                    .lastName("Sharma")
                    .role(residentRole)
                    .emailVerified(false)
                    .accountLocked(false)
                    .failedLoginAttempts(0)
                    .build();
            unverifiedUser.setStatus(Status.PENDING);
            ReflectionTestUtils.setField(unverifiedUser, "id", UUID.randomUUID());

            LoginRequest request = new LoginRequest("priya@example.com", "Secure@123");

            when(userRepository.findByEmailAndIsDeletedFalse("priya@example.com"))
                    .thenReturn(Optional.of(unverifiedUser));
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
            when(httpRequest.getHeader("User-Agent")).thenReturn("TestAgent");
            when(userLoginAttemptRepository.save(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(request, httpRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("not been verified");
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when account is locked")
        void login_withLockedAccount_shouldThrowUnauthorizedException() {
            User lockedUser = User.builder()
                    .email("priya@example.com")
                    .passwordHash("$2a$12$hash")
                    .firstName("Priya")
                    .lastName("Sharma")
                    .role(residentRole)
                    .emailVerified(true)
                    .accountLocked(true)
                    .accountLockedUntil(java.time.LocalDateTime.now().plusMinutes(20))
                    .failedLoginAttempts(5)
                    .build();
            lockedUser.setStatus(Status.ACTIVE);
            ReflectionTestUtils.setField(lockedUser, "id", UUID.randomUUID());

            LoginRequest request = new LoginRequest("priya@example.com", "Secure@123");

            when(userRepository.findByEmailAndIsDeletedFalse("priya@example.com"))
                    .thenReturn(Optional.of(lockedUser));
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
            when(httpRequest.getHeader("User-Agent")).thenReturn("TestAgent");
            when(userLoginAttemptRepository.save(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(request, httpRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("locked");
        }
    }

    // ── Forgot Password Tests ──────────────────────────────────

    @Nested
    @DisplayName("forgotPassword()")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should return generic message for non-existent email (prevent enumeration)")
        void forgotPassword_withNonExistentEmail_shouldReturnGenericMessage() {
            var request = new com.aquatrack.dto.request.ForgotPasswordRequest("unknown@example.com");

            when(userRepository.findByEmailAndIsDeletedFalse("unknown@example.com"))
                    .thenReturn(Optional.empty());
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            String result = authService.forgotPassword(request, httpRequest);

            assertThat(result).contains("If an account with that email exists");
            verify(emailService, never()).sendPasswordResetEmail(any(), anyString());
        }

        @Test
        @DisplayName("Should send reset email for known email")
        void forgotPassword_withValidEmail_shouldSendResetEmail() {
            var request = new com.aquatrack.dto.request.ForgotPasswordRequest("priya@example.com");

            when(userRepository.findByEmailAndIsDeletedFalse("priya@example.com"))
                    .thenReturn(Optional.of(activeUser));
            doNothing().when(passwordResetTokenRepository)
                    .invalidatePreviousTokensForUser(any(UUID.class));
            when(passwordResetTokenRepository.save(any())).thenReturn(null);
            doNothing().when(emailService).sendPasswordResetEmail(any(), anyString());
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
            when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            String result = authService.forgotPassword(request, httpRequest);

            assertThat(result).contains("If an account with that email exists");
            verify(emailService).sendPasswordResetEmail(eq(activeUser), anyString());
        }
    }

    // ── Reset Password Tests ───────────────────────────────────

    @Nested
    @DisplayName("resetPassword()")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should throw BadRequestException when passwords don't match")
        void resetPassword_withMismatchedPasswords_shouldThrow() {
            var request = com.aquatrack.dto.request.ResetPasswordRequest.builder()
                    .token("valid-token")
                    .newPassword("NewPass@123")
                    .confirmPassword("DifferentPass@123")
                    .build();

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("do not match");
        }
    }
}
