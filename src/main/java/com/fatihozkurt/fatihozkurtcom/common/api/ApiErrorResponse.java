package com.fatihozkurt.fatihozkurtcom.common.api;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents a standard API error payload.
 *
 * @param code application specific error code
 * @param message localized human-readable message
 * @param path request path
 * @param timestamp response timestamp
 * @param fieldErrors optional field level validation errors
 */
public record ApiErrorResponse(
        String code,
        String message,
        String path,
        OffsetDateTime timestamp,
        List<FieldErrorItem> fieldErrors
) {
    /**
     * Represents a field level validation error.
     *
     * @param field field name
     * @param message field validation message
     */
    public record FieldErrorItem(String field, String message) {
    }
}
