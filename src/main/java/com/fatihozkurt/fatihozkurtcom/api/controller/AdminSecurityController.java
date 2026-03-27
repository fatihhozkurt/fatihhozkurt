package com.fatihozkurt.fatihozkurtcom.api.controller;

import com.fatihozkurt.fatihozkurtcom.api.dto.admin.AdminAuditResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.SecurityEventResponse;
import com.fatihozkurt.fatihozkurtcom.service.analytics.AnalyticsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes security and audit focused admin endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/security")
@RequiredArgsConstructor
public class AdminSecurityController {

    private final AnalyticsService analyticsService;

    /**
     * Returns recent failed and successful security events.
     *
     * @return event list
     */
    @GetMapping("/events")
    public List<SecurityEventResponse> getSecurityEvents() {
        return analyticsService.getRecentSecurityEvents();
    }

    /**
     * Returns failed login events.
     *
     * @return event list
     */
    @GetMapping("/failed-logins")
    public List<SecurityEventResponse> getFailedLogins() {
        return analyticsService.getFailedLoginEvents();
    }

    /**
     * Returns password reset related events.
     *
     * @return event list
     */
    @GetMapping("/reset-events")
    public List<SecurityEventResponse> getResetEvents() {
        return analyticsService.getResetEvents();
    }

    /**
     * Returns recent admin audit events.
     *
     * @return audit list
     */
    @GetMapping("/audit-events")
    public List<AdminAuditResponse> getAuditEvents() {
        return analyticsService.getRecentAuditEvents();
    }
}
