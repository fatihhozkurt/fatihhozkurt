package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents create or full update request for tech stack item.
 *
 * @param name display name
 * @param iconName icon key
 * @param category category
 * @param sortOrder order
 * @param active active flag
 */
public record TechStackUpsertRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 120) String iconName,
        @Size(max = 80) String category,
        int sortOrder,
        boolean active
) {
}
