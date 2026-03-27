package com.fatihozkurt.fatihozkurtcom.api.controller;

import com.fatihozkurt.fatihozkurtcom.api.dto.admin.DashboardOverviewResponse;
import com.fatihozkurt.fatihozkurtcom.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes dashboard alias endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AnalyticsService analyticsService;

    /**
     * Returns dashboard overview metrics.
     *
     * @return overview response
     */
    @GetMapping("/overview")
    public DashboardOverviewResponse getOverview() {
        return analyticsService.getOverview();
    }
}
