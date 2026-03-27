package com.fatihozkurt.fatihozkurtcom.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Represents reset-password completion payload.
 *
 * @param token reset token
 * @param newPassword new raw password
 */
public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank
        @Pattern(regexp = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\d]).{8,}$", message = "{validation.password.policy}")
        String newPassword
) {
}
