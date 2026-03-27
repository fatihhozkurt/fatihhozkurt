package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import java.time.LocalDate;

/**
 * Represents day-based metric response.
 *
 * @param date metric date
 * @param count metric count
 */
public record DailyMetricResponse(
        LocalDate date,
        long count
) {
}
