package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents security event response item.
 *
 * @param id identifier
 * @param eventType type
 * @param severity severity
 * @param username username
 * @param ipAddress ip address
 * @param details details
 * @param occurredAt timestamp
 */
public record SecurityEventResponse(
        UUID id,
        String eventType,
        String severity,
        String username,
        String ipAddress,
        String details,
        OffsetDateTime occurredAt
) {
}
