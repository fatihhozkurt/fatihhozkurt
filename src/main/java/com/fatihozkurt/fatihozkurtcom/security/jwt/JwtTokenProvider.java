package com.fatihozkurt.fatihozkurtcom.security.jwt;

import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Creates and validates JWT access tokens.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AppProperties appProperties;

    /**
     * Generates a signed access token for an admin user.
     *
     * @param adminUser admin user
     * @return token payload
     */
    public AccessTokenPayload generateAccessToken(AdminUser adminUser) {
        long expiresIn = appProperties.getJwt().getAccessExpirationMinutes() * 60;
        Instant now = Instant.now();
        Instant expiresAt = now.plus(appProperties.getJwt().getAccessExpirationMinutes(), ChronoUnit.MINUTES);
        String token = Jwts.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .subject(adminUser.getUsername())
                .claim("role", adminUser.getRole().name())
                .claim("uid", adminUser.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSigningKey())
                .compact();
        return new AccessTokenPayload(token, expiresIn);
    }

    /**
     * Validates token signature and expiration.
     *
     * @param token jwt token
     * @return true when valid
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extracts username from token subject.
     *
     * @param token jwt token
     * @return username
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts role claim from token.
     *
     * @param token jwt token
     * @return role
     */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] input = appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        byte[] bytes = input.length >= 32 ? input : sha256(input);
        return Keys.hmacShaKeyFor(bytes);
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
