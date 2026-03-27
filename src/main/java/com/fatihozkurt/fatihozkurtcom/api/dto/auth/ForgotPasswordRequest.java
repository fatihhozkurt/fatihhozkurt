package com.fatihozkurt.fatihozkurtcom.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents forgot-password request payload.
 *
 * @param email admin mailbox
 */
public record ForgotPasswordRequest(
        @NotBlank @Email String email
) {
}
