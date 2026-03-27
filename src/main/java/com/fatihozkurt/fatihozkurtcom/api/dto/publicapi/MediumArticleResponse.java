package com.fatihozkurt.fatihozkurtcom.api.dto.publicapi;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents Medium article card response.
 *
 * @param id identifier
 * @param title title
 * @param excerpt excerpt
 * @param href medium url
 * @param readingTime reading duration
 * @param publishedAt publication date
 */
public record MediumArticleResponse(
        UUID id,
        String title,
        String excerpt,
        String href,
        String readingTime,
        LocalDate publishedAt
) {
}
