package com.fatihozkurt.fatihozkurtcom.api.controller;

import com.fatihozkurt.fatihozkurtcom.api.dto.auth.AuthTokenResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.auth.ForgotPasswordRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.auth.LoginRequest;
import com.fatihozkurt.fatihozkurtcom.api.dto.auth.ResetPasswordRequest;
import com.fatihozkurt.fatihozkurtcom.common.web.RequestMetadataService;
import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import com.fatihozkurt.fatihozkurtcom.security.auth.RefreshCookieService;
import com.fatihozkurt.fatihozkurtcom.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes authentication and session related endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RequestMetadataService requestMetadataService;
    private final RefreshCookieService refreshCookieService;
    private final AppProperties appProperties;

    /**
     * Authenticates admin credentials.
     *
     * @param request login payload
     * @param httpServletRequest request
     * @param response response
     * @return access token response
     */
    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest, HttpServletResponse response) {
        AuthService.AuthTokens tokens = authService.login(
                request,
                requestMetadataService.resolveIp(httpServletRequest),
                requestMetadataService.resolveUserAgent(httpServletRequest)
        );
        refreshCookieService.write(response, tokens.refreshToken(), Duration.ofDays(appProperties.getJwt().getRefreshExpirationDays()));
        return tokens.accessToken();
    }

    /**
     * Rotates refresh token and issues new access token.
     *
     * @param request request
     * @param response response
     * @return access token response
     */
    @PostMapping("/refresh")
    public AuthTokenResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = refreshCookieService.read(request);
        AuthService.AuthTokens tokens = authService.refresh(
                refreshToken,
                requestMetadataService.resolveIp(request),
                requestMetadataService.resolveUserAgent(request)
        );
        refreshCookieService.write(response, tokens.refreshToken(), Duration.ofDays(appProperties.getJwt().getRefreshExpirationDays()));
        return tokens.accessToken();
    }

    /**
     * Revokes refresh session and clears refresh cookie.
     *
     * @param request request
     * @param response response
     * @param authentication authentication
     * @return status payload
     */
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = refreshCookieService.read(request);
        String username = authentication != null ? String.valueOf(authentication.getPrincipal()) : "anonymous";
        authService.logout(refreshToken, username, requestMetadataService.resolveIp(request));
        refreshCookieService.clear(response);
        return Map.of("status", "logged_out");
    }

    /**
     * Creates password reset request.
     *
     * @param request forgot-password payload
     * @param httpServletRequest request
     * @return status payload
     */
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpServletRequest) {
        authService.forgotPassword(request, requestMetadataService.resolveIp(httpServletRequest));
        return Map.of("status", "accepted");
    }

    /**
     * Completes password reset.
     *
     * @param request reset payload
     * @param httpServletRequest request
     * @return status payload
     */
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpServletRequest) {
        authService.resetPassword(request, requestMetadataService.resolveIp(httpServletRequest));
        return Map.of("status", "updated");
    }

    /**
     * Returns active CSRF token for browser clients.
     *
     * @param token token
     * @return token payload
     */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of(
                "token", token.getToken(),
                "headerName", token.getHeaderName(),
                "parameterName", token.getParameterName()
        );
    }
}
