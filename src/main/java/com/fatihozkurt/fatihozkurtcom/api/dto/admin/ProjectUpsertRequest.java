package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import com.fatihozkurt.fatihozkurtcom.validation.OptionalUrl;
import com.fatihozkurt.fatihozkurtcom.validation.OptionalUrlOrObjectKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents create or full update request for project item.
 *
 * @param title title
 * @param category category
 * @param summary summary
 * @param repositoryUrl repository url
 * @param demoUrl demo url
 * @param readmeMarkdown markdown content
 * @param coverImageUrl cover image
 * @param stackCsv csv stack
 * @param sortOrder order
 * @param active active flag
 */
public record ProjectUpsertRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 80) String category,
        @NotBlank @Size(max = 1500) String summary,
        @OptionalUrl String repositoryUrl,
        @OptionalUrl String demoUrl,
        @Size(max = 10000) String readmeMarkdown,
        @OptionalUrlOrObjectKey String coverImageUrl,
        @Size(max = 1200) String stackCsv,
        int sortOrder,
        boolean active
) {
}
