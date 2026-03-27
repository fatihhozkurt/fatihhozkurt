package com.fatihozkurt.fatihozkurtcom.security.jwt;

/**
 * Represents generated access token metadata.
 *
 * @param token jwt token
 * @param expiresInSeconds seconds to expire
 */
public record AccessTokenPayload(
        String token,
        long expiresInSeconds
) {
}
