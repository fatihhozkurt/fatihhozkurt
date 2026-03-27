package com.fatihozkurt.fatihozkurtcom.api.dto.publicapi;

import java.util.List;
import java.util.UUID;

/**
 * Represents project detail response.
 *
 * @param id identifier
 * @param title title
 * @param category category
 * @param summary summary
 * @param repositoryUrl repository url
 * @param demoUrl demo url
 * @param readmeMarkdown markdown content
 * @param coverImageUrl cover image
 * @param stack stack list
 */
public record ProjectDetailResponse(
        UUID id,
        String title,
        String category,
        String summary,
        String repositoryUrl,
        String demoUrl,
        String readmeMarkdown,
        String coverImageUrl,
        List<String> stack
) {
}
