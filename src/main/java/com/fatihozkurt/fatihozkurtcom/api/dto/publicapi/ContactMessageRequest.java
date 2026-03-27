package com.fatihozkurt.fatihozkurtcom.api.dto.publicapi;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents public contact form request.
 *
 * @param title message title
 * @param email sender email
 * @param content message content
 */
public record ContactMessageRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(max = 4000) String content
) {
}
