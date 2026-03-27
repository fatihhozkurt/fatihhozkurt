package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import com.fatihozkurt.fatihozkurtcom.validation.OptionalNotBlank;
import com.fatihozkurt.fatihozkurtcom.validation.OptionalSize;

/**
 * Represents partial update request for hero content.
 *
 * @param welcomeText welcome text
 * @param fullName full name
 * @param title role title
 * @param description description
 * @param ctaLabel cta label
 */
public record HeroUpdateRequest(
        @OptionalNotBlank @OptionalSize(max = 180) String welcomeText,
        @OptionalNotBlank @OptionalSize(max = 120) String fullName,
        @OptionalNotBlank @OptionalSize(max = 120) String title,
        @OptionalNotBlank @OptionalSize(max = 1200) String description,
        @OptionalNotBlank @OptionalSize(max = 80) String ctaLabel
) {
}
