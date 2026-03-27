package com.fatihozkurt.fatihozkurtcom.api.dto.publicapi;

import java.util.List;
import java.util.UUID;

/**
 * Represents project summary card response.
 *
 * @param id identifier
 * @param title title
 * @param category category
 * @param summary summary
 * @param repositoryUrl repository url
 * @param coverImageUrl cover image
 * @param stack stack items
 */
public record ProjectSummaryResponse(
        UUID id,
        String title,
        String category,
        String summary,
        String repositoryUrl,
        String coverImageUrl,
        List<String> stack
) {
}
