package com.fatihozkurt.fatihozkurtcom.api.dto.publicapi;

/**
 * Represents public about content response.
 *
 * @param eyebrow eyebrow text
 * @param title section title
 * @param description section description
 */
public record AboutResponse(
        String eyebrow,
        String title,
        String description
) {
}
