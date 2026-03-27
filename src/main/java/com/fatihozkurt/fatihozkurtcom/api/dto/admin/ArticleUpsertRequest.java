package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import com.fatihozkurt.fatihozkurtcom.validation.OptionalUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Represents create or full update request for article item.
 *
 * @param title title
 * @param excerpt excerpt
 * @param href medium link
 * @param readingTime reading time
 * @param publishedAt publish date
 * @param sortOrder order
 * @param active active flag
 */
public record ArticleUpsertRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 1500) String excerpt,
        @OptionalUrl String href,
        @Size(max = 80) String readingTime,
        LocalDate publishedAt,
        int sortOrder,
        boolean active
) {
}
