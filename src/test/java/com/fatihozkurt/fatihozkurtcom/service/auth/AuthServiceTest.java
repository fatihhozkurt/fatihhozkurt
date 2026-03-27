package com.fatihozkurt.fatihozkurtcom.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fatihozkurt.fatihozkurtcom.api.dto.auth.ForgotPasswordRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.auth.LoginRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.auth.ResetPasswordRequest;
import com.fatihozkurt.fatihozkurtcom.common.exception.AppException;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminRole;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminUser;
import com.fatihozkurt.fatihozkurtcom.domain.entity.PasswordResetToken;
import com.fatihozkurt.fatihozkurtcom.domain.entity.RefreshTokenSession;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEventType;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecuritySeverity;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AdminUserRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.PasswordResetTokenRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.RefreshTokenSessionRepository;
import com.fatihozkurt.fatihozkurtcom.security.TokenHashService;
import com.fatihozkurt.fatihozkurtcom.security.jwt.AccessTokenPayload;
import com.fatihozkurt.fatihozkurtcom.security.jwt.JwtTokenProvider;
import com.fatihozkurt.fatihozkurtcom.security.ratelimit.RateLimitService;
import com.fatihozkurt.fatihozkurtcom.service.SecurityEventService;
import com.fatihozkurt.fatihozkurtcom.service.mail.MailDeliveryService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for {@link AuthService}.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;
    @Mock
    private RefreshTokenSessionRepository refreshTokenSessionRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenHashService tokenHashService;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private SecurityEventService securityEventService;
    @Mock
    private MailDeliveryService mailDeliveryService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getJwt().setRefreshExpirationDays(14);
        authService = new AuthService(
                adminUserRepository,
                refreshTokenSessionRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                tokenHashService,
                rateLimitService,
                securityEventService,
                mailDeliveryService,
                appProperties
        );
    }

    @Test
    void loginShouldReturnTokensWhenCredentialsAreValid() {
        AdminUser adminUser = adminUser("fatih.admin", "fatih@example.com", "encoded");
        LoginRequest request = new LoginRequest("fatih.admin", "Admin123!");

        when(adminUserRepository.findByUsernameIgnoreCase("fatih.admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("Admin123!", "encoded")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(adminUser)).thenReturn(new AccessTokenPayload("access-token", 900));
        when(tokenHashService.hash(any())).thenReturn("hashed-refresh");

        AuthService.AuthTokens tokens = authService.login(request, "10.0.0.10", "JUnit");

        assertThat(tokens.accessToken().accessToken()).isEqualTo("access-token");
        assertThat(tokens.accessToken().username()).isEqualTo("fatih.admin");
        assertThat(tokens.refreshToken()).isNotBlank();

        ArgumentCaptor<RefreshTokenSession> sessionCaptor = ArgumentCaptor.forClass(RefreshTokenSession.class);
        verify(refreshTokenSessionRepository).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getAdminUser()).isEqualTo(adminUser);
        assertThat(sessionCaptor.getValue().getExpiresAt()).isAfter(OffsetDateTime.now().minusMinutes(1));
        verify(rateLimitService).checkLogin("10.0.0.10:fatih.admin");
        verify(securityEventService).log(
                eq(SecurityEventType.LOGIN_SUCCESS),
                eq(SecuritySeverity.INFO),
                eq("fatih.admin"),
                eq("10.0.0.10"),
                eq("Authentication successful")
        );
    }

    @Test
    void loginShouldThrowAuth001WhenPasswordIsInvalid() {
        AdminUser adminUser = adminUser("fatih.admin", "fatih@example.com", "encoded");
        LoginRequest request = new LoginRequest("fatih.admin", "wrong-password");

        when(adminUserRepository.findByUsernameIgnoreCase("fatih.admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("wrong-password", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request, "10.0.0.11", "JUnit"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH001);

        verify(securityEventService).log(
                eq(SecurityEventType.LOGIN_FAILED),
                eq(SecuritySeverity.WARN),
                eq("fatih.admin"),
                eq("10.0.0.11"),
                eq("Password mismatch")
        );
        verify(refreshTokenSessionRepository, never()).save(any(RefreshTokenSession.class));
    }

    @Test
    void refreshShouldRotateTokensWhenRefreshSessionIsValid() {
        AdminUser adminUser = adminUser("fatih.admin", "fatih@example.com", "encoded");
        RefreshTokenSession existing = new RefreshTokenSession();
        existing.setAdminUser(adminUser);
        existing.setTokenHash("old-hash");
        existing.setExpiresAt(OffsetDateTime.now().plusMinutes(10));

        when(tokenHashService.hash(any())).thenReturn("old-hash", "new-hash");
        when(refreshTokenSessionRepository.findByTokenHashAndRevokedAtIsNull("old-hash")).thenReturn(Optional.of(existing));
        when(jwtTokenProvider.generateAccessToken(adminUser)).thenReturn(new AccessTokenPayload("new-access", 900));

        AuthService.AuthTokens tokens = authService.refresh("raw-old", "10.0.0.12", "JUnit");

        assertThat(tokens.accessToken().accessToken()).isEqualTo("new-access");
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(existing.getRevokedAt()).isNotNull();

        verify(refreshTokenSessionRepository, times(2)).save(any(RefreshTokenSession.class));
        verify(securityEventService).log(
                eq(SecurityEventType.TOKEN_REFRESHED),
                eq(SecuritySeverity.INFO),
                eq("fatih.admin"),
                eq("10.0.0.12"),
                eq("Refresh token rotated")
        );
    }

    @Test
    void refreshShouldThrowAuth003WhenRefreshTokenIsMissing() {
        assertThatThrownBy(() -> authService.refresh("  ", "10.0.0.13", "JUnit"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH003);
    }

    @Test
    void forgotPasswordShouldIgnoreUnknownEmail() {
        when(adminUserRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("unknown@example.com"), "10.0.0.14");

        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(mailDeliveryService, never()).sendPasswordReset(any(), any());
        verify(securityEventService).log(
                eq(SecurityEventType.RESET_REQUESTED),
                eq(SecuritySeverity.WARN),
                eq(null),
                eq("10.0.0.14"),
                eq("Reset requested for unknown email")
        );
    }

    @Test
    void forgotPasswordShouldCreateTokenAndSendMailForKnownEmail() {
        AdminUser adminUser = adminUser("fatih.admin", "fatih@example.com", "encoded");
        when(adminUserRepository.findByEmailIgnoreCase("fatih@example.com")).thenReturn(Optional.of(adminUser));
        when(tokenHashService.hash(any())).thenReturn("reset-hash");

        authService.forgotPassword(new ForgotPasswordRequest("fatih@example.com"), "10.0.0.15");

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(mailDeliveryService).sendPasswordReset(eq("fatih@example.com"), any(String.class));
    }

    @Test
    void resetPasswordShouldThrowAuth005WhenPolicyFails() {
        ResetPasswordRequest request = new ResetPasswordRequest("token-raw", "weakpass");

        assertThatThrownBy(() -> authService.resetPassword(request, "10.0.0.16"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH005);
    }

    @Test
    void resetPasswordShouldUpdatePasswordAndRevokeSessionsWhenTokenIsValid() {
        AdminUser adminUser = adminUser("fatih.admin", "fatih@example.com", "old-hash");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setAdminUser(adminUser);
        resetToken.setTokenHash("hashed-token");
        resetToken.setExpiresAt(OffsetDateTime.now().plusMinutes(10));

        when(tokenHashService.hash("raw-token")).thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull("hashed-token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("NewAdmin123!")).thenReturn("new-hash");

        authService.resetPassword(new ResetPasswordRequest("raw-token", "NewAdmin123!"), "10.0.0.17");

        assertThat(adminUser.getPasswordHash()).isEqualTo("new-hash");
        assertThat(resetToken.getUsedAt()).isNotNull();
        verify(adminUserRepository).save(adminUser);
        verify(passwordResetTokenRepository).save(resetToken);
        verify(refreshTokenSessionRepository).revokeAllByAdminUser(eq(adminUser), any(OffsetDateTime.class));
    }

    private AdminUser adminUser(String username, String email, String passwordHash) {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername(username);
        adminUser.setEmail(email);
        adminUser.setPasswordHash(passwordHash);
        adminUser.setRole(AdminRole.ROLE_ADMIN);
        adminUser.setActive(true);
        return adminUser;
    }
}
