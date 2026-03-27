package com.fatihozkurt.fatihozkurtcom.api.dto.auth;

/**
 * Represents access token response.
 *
 * @param accessToken jwt access token
 * @param tokenType token type
 * @param expiresIn seconds to expire
 * @param username authenticated username
 */
public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String username
) {
}
