package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

/**
 * Represents admin dashboard summary metrics.
 *
 * @param visitsToday visit count
 * @param failedLoginsToday failed login count
 * @param securityEventsToday security event count
 * @param contactMessagesToday contact message count
 */
public record DashboardOverviewResponse(
        long visitsToday,
        long failedLoginsToday,
        long securityEventsToday,
        long contactMessagesToday
) {
}
