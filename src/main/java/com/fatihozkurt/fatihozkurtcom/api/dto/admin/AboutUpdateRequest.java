package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import com.fatihozkurt.fatihozkurtcom.validation.OptionalNotBlank;
import com.fatihozkurt.fatihozkurtcom.validation.OptionalSize;

/**
 * Represents partial update request for about section.
 *
 * @param eyebrow eyebrow text
 * @param title title text
 * @param description description text
 */
public record AboutUpdateRequest(
        @OptionalNotBlank @OptionalSize(max = 80) String eyebrow,
        @OptionalNotBlank @OptionalSize(max = 160) String title,
        @OptionalNotBlank @OptionalSize(max = 1200) String description
) {
}
