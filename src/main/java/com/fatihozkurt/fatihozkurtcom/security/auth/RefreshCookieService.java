package com.fatihozkurt.fatihozkurtcom.security.auth;

import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Manages refresh token cookie lifecycle.
 */
@Component
@RequiredArgsConstructor
public class RefreshCookieService {

    private final AppProperties appProperties;

    /**
     * Writes refresh token cookie.
     *
     * @param response response
     * @param token refresh token
     * @param maxAge max age
     */
    public void write(HttpServletResponse response, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(cookieName(), token)
                .httpOnly(true)
                .secure(appProperties.getSecurity().getCookies().isSecure())
                .path("/")
                .sameSite(appProperties.getSecurity().getCookies().getSameSite())
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Clears refresh token cookie.
     *
     * @param response response
     */
    public void clear(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName(), "")
                .httpOnly(true)
                .secure(appProperties.getSecurity().getCookies().isSecure())
                .path("/")
                .sameSite(appProperties.getSecurity().getCookies().getSameSite())
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Reads refresh token cookie.
     *
     * @param request request
     * @return token or null
     */
    public String read(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String cookieName() {
        return appProperties.getSecurity().getCookies().getRefreshCookieName();
    }
}
