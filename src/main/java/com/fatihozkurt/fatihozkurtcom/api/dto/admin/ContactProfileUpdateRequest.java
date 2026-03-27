package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import com.fatihozkurt.fatihozkurtcom.validation.OptionalEmail;
import com.fatihozkurt.fatihozkurtcom.validation.OptionalNotBlank;
import com.fatihozkurt.fatihozkurtcom.validation.OptionalSize;
import com.fatihozkurt.fatihozkurtcom.validation.OptionalUrl;

/**
 * Represents partial update request for contact profile.
 *
 * @param email public email
 * @param linkedinUrl linkedin url
 * @param githubUrl github url
 * @param mediumUrl medium url
 * @param recipientEmail recipient mailbox for form deliveries
 */
public record ContactProfileUpdateRequest(
        @OptionalEmail @OptionalSize(max = 180) String email,
        @OptionalUrl String linkedinUrl,
        @OptionalUrl String githubUrl,
        @OptionalUrl String mediumUrl,
        @OptionalNotBlank @OptionalEmail @OptionalSize(max = 180) String recipientEmail
) {
}
