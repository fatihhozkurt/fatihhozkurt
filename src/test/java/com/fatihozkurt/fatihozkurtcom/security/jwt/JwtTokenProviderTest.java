package com.fatihozkurt.fatihozkurtcom.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminRole;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminUser;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JwtTokenProvider}.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getJwt().setIssuer("fatihozkurtcom-test");
        appProperties.getJwt().setAccessExpirationMinutes(15);
        appProperties.getJwt().setSecret("short-secret-for-sha256-path");
        jwtTokenProvider = new JwtTokenProvider(appProperties);
    }

    @Test
    void generateAccessTokenShouldCreateValidTokenWithClaims() {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("fatih.admin");
        adminUser.setRole(AdminRole.ROLE_ADMIN);

        AccessTokenPayload payload = jwtTokenProvider.generateAccessToken(adminUser);

        assertThat(payload.token()).isNotBlank();
        assertThat(payload.expiresInSeconds()).isEqualTo(900);
        assertThat(jwtTokenProvider.isValid(payload.token())).isTrue();
        assertThat(jwtTokenProvider.getUsername(payload.token())).isEqualTo("fatih.admin");
        assertThat(jwtTokenProvider.getRole(payload.token())).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void isValidShouldReturnFalseForMalformedToken() {
        assertThat(jwtTokenProvider.isValid("malformed.token.value")).isFalse();
    }
}
