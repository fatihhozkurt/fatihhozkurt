package com.fatihozkurt.fatihozkurtcom.service.analytics;

import com.fatihozkurt.fatihozkurtcom.api.dto.admin.AdminAuditResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.CountryDistributionResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.DailyMetricResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.DashboardOverviewResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.PathDistributionResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.SecurityEventResponse;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEventType;
import com.fatihozkurt.fatihozkurtcom.domain.entity.VisitEvent;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AdminAuditEventRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ContactMessageRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.MailDeliveryLogRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.SecurityEventRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.VisitEventRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides analytics and observability data for admin dashboard.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final VisitEventRepository visitEventRepository;
    private final SecurityEventRepository securityEventRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final AdminAuditEventRepository adminAuditEventRepository;
    private final MailDeliveryLogRepository mailDeliveryLogRepository;

    /**
     * Returns dashboard high-level counters for last 24 hours.
     *
     * @return overview response
     */
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview() {
        OffsetDateTime after = OffsetDateTime.now().minusHours(24);
        long visits = visitEventRepository.countByOccurredAtAfter(after);
        long failedLogins = securityEventRepository.countByEventTypeAndOccurredAtAfter(SecurityEventType.LOGIN_FAILED, after);
        long security = securityEventRepository.countByOccurredAtAfter(after);
        long contact = contactMessageRepository.countByCreatedAtAfter(after);
        return new DashboardOverviewResponse(visits, failedLogins, security, contact);
    }

    /**
     * Returns visit trend for requested day window.
     *
     * @param days number of days
     * @return trend list
     */
    @Transactional(readOnly = true)
    public List<DailyMetricResponse> getVisitTrend(int days) {
        int normalizedDays = Math.max(1, Math.min(days, 90));
        OffsetDateTime after = OffsetDateTime.now().minusDays(normalizedDays);
        Map<LocalDate, Long> map = visitEventRepository.findByOccurredAtAfter(after).stream()
                .collect(Collectors.groupingBy(event -> event.getOccurredAt().toLocalDate(), Collectors.counting()));
        LocalDate start = LocalDate.now().minusDays(normalizedDays - 1L);
        return start.datesUntil(LocalDate.now().plusDays(1))
                .map(date -> new DailyMetricResponse(date, map.getOrDefault(date, 0L)))
                .toList();
    }

    /**
     * Returns country distribution list for last 30 days.
     *
     * @return country list
     */
    @Transactional(readOnly = true)
    public List<CountryDistributionResponse> getCountryDistribution() {
        OffsetDateTime after = OffsetDateTime.now().minusDays(30);
        return visitEventRepository.countCountries(after).stream()
                .map(row -> new CountryDistributionResponse(
                        row[0] == null ? "Unknown" : String.valueOf(row[0]),
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }

    /**
     * Returns top visited paths for last 30 days.
     *
     * @return path distribution list
     */
    @Transactional(readOnly = true)
    public List<PathDistributionResponse> getTopPages() {
        OffsetDateTime after = OffsetDateTime.now().minusDays(30);
        return visitEventRepository.countPaths(after).stream()
                .map(row -> new PathDistributionResponse(
                        row[0] == null ? "/" : String.valueOf(row[0]),
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }

    /**
     * Returns latest security events.
     *
     * @return list
     */
    @Transactional(readOnly = true)
    public List<SecurityEventResponse> getRecentSecurityEvents() {
        return securityEventRepository.findTop50ByOrderByOccurredAtDesc().stream()
                .map(event -> new SecurityEventResponse(
                        event.getId(),
                        event.getEventType().name(),
                        event.getSeverity().name(),
                        event.getUsername(),
                        event.getIpAddress(),
                        event.getDetails(),
                        event.getOccurredAt()
                ))
                .toList();
    }

    /**
     * Returns failed login events.
     *
     * @return failed login event list
     */
    @Transactional(readOnly = true)
    public List<SecurityEventResponse> getFailedLoginEvents() {
        return getRecentSecurityEvents().stream()
                .filter(event -> SecurityEventType.LOGIN_FAILED.name().equals(event.eventType()))
                .toList();
    }

    /**
     * Returns reset-related security events.
     *
     * @return reset event list
     */
    @Transactional(readOnly = true)
    public List<SecurityEventResponse> getResetEvents() {
        return getRecentSecurityEvents().stream()
                .filter(event -> SecurityEventType.RESET_REQUESTED.name().equals(event.eventType())
                        || SecurityEventType.RESET_COMPLETED.name().equals(event.eventType()))
                .toList();
    }

    /**
     * Returns latest admin audit events.
     *
     * @return list
     */
    @Transactional(readOnly = true)
    public List<AdminAuditResponse> getRecentAuditEvents() {
        return adminAuditEventRepository.findTop100ByOrderByOccurredAtDesc().stream()
                .map(event -> new AdminAuditResponse(
                        event.getId(),
                        event.getActor(),
                        event.getAction(),
                        event.getResource(),
                        event.getDetails(),
                        event.getOccurredAt()
                ))
                .toList();
    }

    /**
     * Returns latest mail delivery logs.
     *
     * @return list
     */
    @Transactional(readOnly = true)
    public List<?> getMailDeliveries() {
        return mailDeliveryLogRepository.findTop100ByOrderByAttemptedAtDesc();
    }

    /**
     * Persists a visit event.
     *
     * @param path path
     * @param country country
     * @param ip ip
     * @param userAgent user agent
     */
    @Transactional
    public void recordVisit(String path, String country, String ip, String userAgent) {
        VisitEvent visitEvent = new VisitEvent();
        visitEvent.setPath(path);
        visitEvent.setCountry(country);
        visitEvent.setIpAddress(ip);
        visitEvent.setUserAgent(userAgent);
        visitEventRepository.save(visitEvent);
    }
}
