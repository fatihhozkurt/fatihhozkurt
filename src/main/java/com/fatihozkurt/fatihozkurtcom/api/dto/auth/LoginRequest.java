package com.fatihozkurt.fatihozkurtcom.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents login request payload.
 *
 * @param username username
 * @param password raw password
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
