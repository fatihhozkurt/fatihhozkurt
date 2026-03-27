package com.fatihozkurt.fatihozkurtcom.api.controller;

import com.fatihozkurt.fatihozkurtcom.api.dto.admin.AdminAuditResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.CountryDistributionResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.DailyMetricResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.DashboardOverviewResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.PathDistributionResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.SecurityEventResponse;
import com.fatihozkurt.fatihozkurtcom.service.analytics.AnalyticsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes admin analytics and observability endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Returns dashboard overview metrics.
     *
     * @return overview
     */
    @GetMapping("/overview")
    public DashboardOverviewResponse getOverview() {
        return analyticsService.getOverview();
    }

    /**
     * Returns country distribution for visits.
     *
     * @return country list
     */
    @GetMapping("/country-distribution")
    public List<CountryDistributionResponse> getCountryDistribution() {
        return analyticsService.getCountryDistribution();
    }

    /**
     * Returns visit trend by day.
     *
     * @param days day window
     * @return trend list
     */
    @GetMapping("/visits-trend")
    public List<DailyMetricResponse> getVisitsTrend(@RequestParam(defaultValue = "7") int days) {
        return analyticsService.getVisitTrend(days);
    }

    /**
     * Returns top visited pages.
     *
     * @return page distribution list
     */
    @GetMapping("/top-pages")
    public List<PathDistributionResponse> getTopPages() {
        return analyticsService.getTopPages();
    }

    /**
     * Returns recent security events.
     *
     * @return event list
     */
    @GetMapping("/security-events")
    public List<SecurityEventResponse> getSecurityEvents() {
        return analyticsService.getRecentSecurityEvents();
    }

    /**
     * Returns recent audit events.
     *
     * @return event list
     */
    @GetMapping("/audit-events")
    public List<AdminAuditResponse> getAuditEvents() {
        return analyticsService.getRecentAuditEvents();
    }

    /**
     * Returns latest mail deliveries.
     *
     * @return delivery list
     */
    @GetMapping("/mail-deliveries")
    public List<?> getMailDeliveries() {
        return analyticsService.getMailDeliveries();
    }
}
