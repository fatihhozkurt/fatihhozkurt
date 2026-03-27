package com.fatihozkurt.fatihozkurtcom.api.dto.publicapi;

import java.util.UUID;

/**
 * Represents public tech stack item response.
 *
 * @param id identifier
 * @param name name
 * @param iconName icon key
 * @param category category
 */
public record TechStackItemResponse(
        UUID id,
        String name,
        String iconName,
        String category
) {
}
