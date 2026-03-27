package com.fatihozkurt.fatihozkurtcom.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminUser;
import com.fatihozkurt.fatihozkurtcom.domain.entity.PasswordResetToken;
import com.fatihozkurt.fatihozkurtcom.domain.repository.PasswordResetTokenRepository;
import com.fatihozkurt.fatihozkurtcom.security.TokenHashService;
import jakarta.servlet.http.Cookie;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration tests for authentication API flows.
 */
class AuthApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private TokenHashService tokenHashService;

    @BeforeEach
    void beforeEach() {
        cleanDatabase();
    }

    @Test
    void loginRefreshLogoutShouldWorkEndToEnd() throws Exception {
        seedAdmin("fatih.admin", "fatih@example.com", "Admin123!");

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", nextIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "fatih.admin",
                                "password", "Admin123!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("fatih.admin"))
                .andReturn();

        String refreshSetCookie = login.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(refreshSetCookie).contains("refresh_token=");
        String refreshTokenValue = refreshSetCookie.split(";", 2)[0].split("=", 2)[1];
        String accessToken = objectMapper.readTree(login.getResponse().getContentAsString()).path("accessToken").asText();
        assertThat(accessToken).isNotBlank();

        MvcResult refresh = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", refreshTokenValue))
                        .header("X-Forwarded-For", nextIp()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        String rotatedCookie = refresh.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(rotatedCookie).contains("refresh_token=");
        String rotatedRefreshToken = rotatedCookie.split(";", 2)[0].split("=", 2)[1];

        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf().asHeader())
                        .cookie(new Cookie("refresh_token", rotatedRefreshToken))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .header("X-Forwarded-For", nextIp()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("logged_out"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    @Test
    void loginShouldReturnAuth001ForInvalidCredentials() throws Exception {
        seedAdmin("fatih.admin", "fatih@example.com", "Admin123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", nextIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "fatih.admin",
                                "password", "Wrong123!"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH001"));
    }

    @Test
    void forgotAndResetPasswordShouldWorkForKnownAdmin() throws Exception {
        AdminUser adminUser = seedAdmin("fatih.admin", "fatih@example.com", "Admin123!");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .header("X-Forwarded-For", nextIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "fatih@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("accepted"));

        assertThat(passwordResetTokenRepository.count()).isEqualTo(1);

        String rawToken = "known-reset-token";
        PasswordResetToken token = new PasswordResetToken();
        token.setAdminUser(adminUser);
        token.setTokenHash(tokenHashService.hash(rawToken));
        token.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
        passwordResetTokenRepository.save(token);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .header("X-Forwarded-For", nextIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", rawToken,
                                "newPassword", "NewAdmin123!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("updated"));

        AdminUser updated = adminUserRepository.findByUsernameIgnoreCase("fatih.admin").orElseThrow();
        assertThat(passwordEncoder.matches("NewAdmin123!", updated.getPasswordHash())).isTrue();
    }

    @Test
    void forgotPasswordShouldBeAcceptedForUnknownEmailWithoutCreatingToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .header("X-Forwarded-For", nextIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "unknown@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("accepted"));

        assertThat(passwordResetTokenRepository.count()).isZero();
    }

    @Test
    void resetPasswordShouldReturnAuth004ForUnknownToken() throws Exception {
        seedAdmin("fatih.admin", "fatih@example.com", "Admin123!");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .header("X-Forwarded-For", nextIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "unknown-token",
                                "newPassword", "NewAdmin123!"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH004"));
    }

    @Test
    void loginShouldReturnSec001AfterRateLimitExceeded() throws Exception {
        seedAdmin("fatih.admin", "fatih@example.com", "Admin123!");
        String sameIp = "10.9."
                + ThreadLocalRandom.current().nextInt(1, 255)
                + "."
                + ThreadLocalRandom.current().nextInt(1, 255);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .header("X-Forwarded-For", sameIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "username", "fatih.admin",
                                    "password", "Wrong123!"
                            ))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("AUTH001"));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", sameIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "fatih.admin",
                                "password", "Wrong123!"
                        ))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("SEC001"));
    }
}
