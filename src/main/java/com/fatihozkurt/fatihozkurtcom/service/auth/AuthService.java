package com.fatihozkurt.fatihozkurtcom.service.auth;

import com.fatihozkurt.fatihozkurtcom.api.dto.auth.AuthTokenResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.auth.ForgotPasswordRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.auth.LoginRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.auth.ResetPasswordRequest;
import com.fatihozkurt.fatihozkurtcom.common.exception.AppException;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
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
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles authentication, refresh, logout, forgot-password and reset flows.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String PASSWORD_POLICY = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\d]).{8,}$";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AdminUserRepository adminUserRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashService tokenHashService;
    private final RateLimitService rateLimitService;
    private final SecurityEventService securityEventService;
    private final MailDeliveryService mailDeliveryService;
    private final AppProperties appProperties;

    /**
     * Authenticates admin and returns access token plus raw refresh token.
     *
     * @param request login request
     * @param ipAddress request ip
     * @param userAgent request user agent
     * @return auth tokens
     */
    @Transactional
    public AuthTokens login(LoginRequest request, String ipAddress, String userAgent) {
        rateLimitService.checkLogin(ipAddress + ":" + request.username().toLowerCase());
        AdminUser adminUser = adminUserRepository.findByUsernameIgnoreCase(request.username())
                .filter(AdminUser::isActive)
                .orElseThrow(() -> {
                    securityEventService.log(SecurityEventType.LOGIN_FAILED, SecuritySeverity.WARN, request.username(), ipAddress, "Unknown username");
                    return new AppException(ErrorCode.AUTH001);
                });

        if (!passwordEncoder.matches(request.password(), adminUser.getPasswordHash())) {
            securityEventService.log(SecurityEventType.LOGIN_FAILED, SecuritySeverity.WARN, request.username(), ipAddress, "Password mismatch");
            throw new AppException(ErrorCode.AUTH001);
        }

        AccessTokenPayload accessTokenPayload = jwtTokenProvider.generateAccessToken(adminUser);
        String rawRefreshToken = generateSecureToken();

        RefreshTokenSession refreshTokenSession = new RefreshTokenSession();
        refreshTokenSession.setAdminUser(adminUser);
        refreshTokenSession.setTokenHash(tokenHashService.hash(rawRefreshToken));
        refreshTokenSession.setUserAgent(userAgent);
        refreshTokenSession.setIpAddress(ipAddress);
        refreshTokenSession.setExpiresAt(OffsetDateTime.now().plusDays(appProperties.getJwt().getRefreshExpirationDays()));
        refreshTokenSessionRepository.save(refreshTokenSession);

        securityEventService.log(SecurityEventType.LOGIN_SUCCESS, SecuritySeverity.INFO, adminUser.getUsername(), ipAddress, "Authentication successful");

        AuthTokenResponse accessTokenResponse = new AuthTokenResponse(
                accessTokenPayload.token(),
                "Bearer",
                accessTokenPayload.expiresInSeconds(),
                adminUser.getUsername()
        );
        return new AuthTokens(accessTokenResponse, rawRefreshToken);
    }

    /**
     * Rotates refresh token and returns a new access/refresh pair.
     *
     * @param rawRefreshToken refresh token from cookie
     * @param ipAddress request ip
     * @param userAgent request user agent
     * @return auth tokens
     */
    @Transactional
    public AuthTokens refresh(String rawRefreshToken, String ipAddress, String userAgent) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AppException(ErrorCode.AUTH003);
        }
        String hash = tokenHashService.hash(rawRefreshToken);
        RefreshTokenSession existing = refreshTokenSessionRepository.findByTokenHashAndRevokedAtIsNull(hash)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH003));
        if (existing.getExpiresAt().isBefore(OffsetDateTime.now())) {
            existing.setRevokedAt(OffsetDateTime.now());
            refreshTokenSessionRepository.save(existing);
            throw new AppException(ErrorCode.AUTH003);
        }

        AdminUser adminUser = existing.getAdminUser();
        existing.setRevokedAt(OffsetDateTime.now());
        refreshTokenSessionRepository.save(existing);

        String newRawRefresh = generateSecureToken();
        RefreshTokenSession rotated = new RefreshTokenSession();
        rotated.setAdminUser(adminUser);
        rotated.setTokenHash(tokenHashService.hash(newRawRefresh));
        rotated.setUserAgent(userAgent);
        rotated.setIpAddress(ipAddress);
        rotated.setExpiresAt(OffsetDateTime.now().plusDays(appProperties.getJwt().getRefreshExpirationDays()));
        refreshTokenSessionRepository.save(rotated);

        AccessTokenPayload accessTokenPayload = jwtTokenProvider.generateAccessToken(adminUser);
        securityEventService.log(SecurityEventType.TOKEN_REFRESHED, SecuritySeverity.INFO, adminUser.getUsername(), ipAddress, "Refresh token rotated");

        AuthTokenResponse accessTokenResponse = new AuthTokenResponse(
                accessTokenPayload.token(),
                "Bearer",
                accessTokenPayload.expiresInSeconds(),
                adminUser.getUsername()
        );
        return new AuthTokens(accessTokenResponse, newRawRefresh);
    }

    /**
     * Revokes active refresh session for logout.
     *
     * @param rawRefreshToken refresh token
     * @param username authenticated username
     * @param ipAddress ip address
     */
    @Transactional
    public void logout(String rawRefreshToken, String username, String ipAddress) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String hash = tokenHashService.hash(rawRefreshToken);
            refreshTokenSessionRepository.findByTokenHashAndRevokedAtIsNull(hash).ifPresent(session -> {
                session.setRevokedAt(OffsetDateTime.now());
                refreshTokenSessionRepository.save(session);
            });
        }
        securityEventService.log(SecurityEventType.LOGOUT, SecuritySeverity.INFO, username, ipAddress, "Session revoked");
    }

    /**
     * Creates password reset token and delivers mail if admin exists.
     *
     * @param request forgot request
     * @param ipAddress ip address
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, String ipAddress) {
        rateLimitService.checkForgotPassword(ipAddress + ":" + request.email().toLowerCase());
        Optional<AdminUser> optionalAdminUser = adminUserRepository.findByEmailIgnoreCase(request.email());
        if (optionalAdminUser.isEmpty()) {
            securityEventService.log(SecurityEventType.RESET_REQUESTED, SecuritySeverity.WARN, null, ipAddress, "Reset requested for unknown email");
            return;
        }

        AdminUser adminUser = optionalAdminUser.get();
        String rawResetToken = generateSecureToken();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setAdminUser(adminUser);
        resetToken.setTokenHash(tokenHashService.hash(rawResetToken));
        resetToken.setExpiresAt(OffsetDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "https://fatihozkurt.com/auth/reset-password?token=" + rawResetToken;
        mailDeliveryService.sendPasswordReset(adminUser.getEmail(), resetLink);
        securityEventService.log(SecurityEventType.RESET_REQUESTED, SecuritySeverity.INFO, adminUser.getUsername(), ipAddress, "Reset token generated");
    }

    /**
     * Validates reset token and updates password securely.
     *
     * @param request reset request
     * @param ipAddress ip address
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request, String ipAddress) {
        if (!request.newPassword().matches(PASSWORD_POLICY)) {
            throw new AppException(ErrorCode.AUTH005);
        }
        String tokenHash = tokenHashService.hash(request.token());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(tokenHash)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH004));
        if (resetToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new AppException(ErrorCode.AUTH004);
        }

        AdminUser adminUser = resetToken.getAdminUser();
        adminUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        adminUserRepository.save(adminUser);

        resetToken.setUsedAt(OffsetDateTime.now());
        passwordResetTokenRepository.save(resetToken);
        refreshTokenSessionRepository.revokeAllByAdminUser(adminUser, OffsetDateTime.now());
        securityEventService.log(SecurityEventType.RESET_COMPLETED, SecuritySeverity.INFO, adminUser.getUsername(), ipAddress, "Password reset completed");
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Represents auth response pair for access and refresh tokens.
     *
     * @param accessToken access token response payload
     * @param refreshToken raw refresh token
     */
    public record AuthTokens(AuthTokenResponse accessToken, String refreshToken) {
    }
}
