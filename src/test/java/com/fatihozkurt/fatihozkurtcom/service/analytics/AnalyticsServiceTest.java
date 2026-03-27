package com.fatihozkurt.fatihozkurtcom.service.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fatihozkurt.fatihozkurtcom.api.dto.admin.CountryDistributionResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.DailyMetricResponse;
import com.fatihozkurt.fatihozkurtcom.api.dto.admin.DashboardOverviewResponse;
import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminAuditEvent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEvent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEventType;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecuritySeverity;
import com.fatihozkurt.fatihozkurtcom.domain.entity.VisitEvent;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AdminAuditEventRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.ContactMessageRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.MailDeliveryLogRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.SecurityEventRepository;
import com.fatihozkurt.fatihozkurtcom.domain.repository.VisitEventRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AnalyticsService}.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private VisitEventRepository visitEventRepository;
    @Mock
    private SecurityEventRepository securityEventRepository;
    @Mock
    private ContactMessageRepository contactMessageRepository;
    @Mock
    private AdminAuditEventRepository adminAuditEventRepository;
    @Mock
    private MailDeliveryLogRepository mailDeliveryLogRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                visitEventRepository,
                securityEventRepository,
                contactMessageRepository,
                adminAuditEventRepository,
                mailDeliveryLogRepository
        );
    }

    @Test
    void getOverviewShouldAggregateLast24HourCounters() {
        when(visitEventRepository.countByOccurredAtAfter(org.mockito.ArgumentMatchers.any())).thenReturn(40L);
        when(securityEventRepository.countByEventTypeAndOccurredAtAfter(org.mockito.ArgumentMatchers.eq(SecurityEventType.LOGIN_FAILED), org.mockito.ArgumentMatchers.any())).thenReturn(3L);
        when(securityEventRepository.countByOccurredAtAfter(org.mockito.ArgumentMatchers.any())).thenReturn(12L);
        when(contactMessageRepository.countByCreatedAtAfter(org.mockito.ArgumentMatchers.any())).thenReturn(5L);

        DashboardOverviewResponse response = analyticsService.getOverview();

        assertThat(response.visitsToday()).isEqualTo(40);
        assertThat(response.failedLoginsToday()).isEqualTo(3);
        assertThat(response.securityEventsToday()).isEqualTo(12);
        assertThat(response.contactMessagesToday()).isEqualTo(5);
    }

    @Test
    void getVisitTrendShouldNormalizeDaysAndFillMissingDates() {
        VisitEvent yesterday = new VisitEvent();
        yesterday.setOccurredAt(OffsetDateTime.now().minusDays(1));
        VisitEvent today = new VisitEvent();
        today.setOccurredAt(OffsetDateTime.now());
        when(visitEventRepository.findByOccurredAtAfter(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(yesterday, today));

        List<DailyMetricResponse> response = analyticsService.getVisitTrend(3);

        assertThat(response).hasSize(3);
        assertThat(response.stream().map(DailyMetricResponse::date)).contains(LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), LocalDate.now());
        assertThat(response.stream().mapToLong(DailyMetricResponse::count).sum()).isEqualTo(2);
    }

    @Test
    void getCountryDistributionShouldMapNullCountryToUnknown() {
        when(visitEventRepository.countCountries(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                new Object[]{"Turkiye", 42L},
                new Object[]{null, 8L}
        ));

        List<CountryDistributionResponse> response = analyticsService.getCountryDistribution();

        assertThat(response).containsExactly(
                new CountryDistributionResponse("Turkiye", 42L),
                new CountryDistributionResponse("Unknown", 8L)
        );
    }

    @Test
    void getFailedAndResetEventsShouldFilterFromRecentSecurityEvents() {
        SecurityEvent failed = new SecurityEvent();
        failed.setEventType(SecurityEventType.LOGIN_FAILED);
        failed.setSeverity(SecuritySeverity.WARN);

        SecurityEvent resetRequested = new SecurityEvent();
        resetRequested.setEventType(SecurityEventType.RESET_REQUESTED);
        resetRequested.setSeverity(SecuritySeverity.INFO);

        SecurityEvent resetCompleted = new SecurityEvent();
        resetCompleted.setEventType(SecurityEventType.RESET_COMPLETED);
        resetCompleted.setSeverity(SecuritySeverity.INFO);

        when(securityEventRepository.findTop50ByOrderByOccurredAtDesc()).thenReturn(List.of(failed, resetRequested, resetCompleted));

        assertThat(analyticsService.getFailedLoginEvents()).hasSize(1);
        assertThat(analyticsService.getResetEvents()).hasSize(2);
    }

    @Test
    void getRecentAuditEventsShouldMapEntitiesToResponse() {
        AdminAuditEvent event = new AdminAuditEvent();
        event.setActor("fatih.admin");
        event.setAction("Updated hero");
        event.setResource("hero");
        event.setDetails("welcome text");
        when(adminAuditEventRepository.findTop100ByOrderByOccurredAtDesc()).thenReturn(List.of(event));

        assertThat(analyticsService.getRecentAuditEvents()).singleElement()
                .satisfies(response -> {
                    assertThat(response.actor()).isEqualTo("fatih.admin");
                    assertThat(response.resource()).isEqualTo("hero");
                });
    }

    @Test
    void recordVisitShouldPersistVisitEvent() {
        analyticsService.recordVisit("/projects", "Turkiye", "10.0.0.33", "JUnit");

        ArgumentCaptor<VisitEvent> captor = ArgumentCaptor.forClass(VisitEvent.class);
        verify(visitEventRepository).save(captor.capture());
        assertThat(captor.getValue().getPath()).isEqualTo("/projects");
        assertThat(captor.getValue().getCountry()).isEqualTo("Turkiye");
        assertThat(captor.getValue().getIpAddress()).isEqualTo("10.0.0.33");
    }
}
