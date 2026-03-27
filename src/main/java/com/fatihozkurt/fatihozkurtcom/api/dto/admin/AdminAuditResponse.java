package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents admin audit event response item.
 *
 * @param id identifier
 * @param actor actor
 * @param action action
 * @param resource resource
 * @param details details
 * @param occurredAt timestamp
 */
public record AdminAuditResponse(
        UUID id,
        String actor,
        String action,
        String resource,
        String details,
        OffsetDateTime occurredAt
) {
}
