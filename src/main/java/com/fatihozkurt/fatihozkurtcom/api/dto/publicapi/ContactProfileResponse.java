package com.fatihozkurt.fatihozkurtcom.api.dto.publicapi;

/**
 * Represents public contact links.
 *
 * @param email email
 * @param linkedinUrl linkedin url
 * @param githubUrl github url
 * @param mediumUrl medium url
 */
public record ContactProfileResponse(
        String email,
        String linkedinUrl,
        String githubUrl,
        String mediumUrl
) {
}
