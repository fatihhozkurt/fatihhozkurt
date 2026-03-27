package com.fatihozkurt.fatihozkurtcom.api.dto.publicapi;

/**
 * Represents public hero content response.
 *
 * @param welcomeText welcome badge text
 * @param fullName full name
 * @param title role title
 * @param description short summary
 * @param ctaLabel call to action label
 */
public record HeroResponse(
        String welcomeText,
        String fullName,
        String title,
        String description,
        String ctaLabel
) {
}
