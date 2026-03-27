package com.fatihozkurt.fatihozkurtcom.service.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import com.fatihozkurt.fatihozkurtcom.domain.entity.MailDeliveryLog;
import com.fatihozkurt.fatihozkurtcom.domain.entity.MailDeliveryStatus;
import com.fatihozkurt.fatihozkurtcom.domain.entity.MailPurpose;
import com.fatihozkurt.fatihozkurtcom.domain.repository.MailDeliveryLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link MailDeliveryService}.
 */
@ExtendWith(MockitoExtension.class)
class MailDeliveryServiceTest {

    @Mock
    private MailSenderPort mailSenderPort;
    @Mock
    private MailDeliveryLogRepository mailDeliveryLogRepository;

    private MailDeliveryService mailDeliveryService;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getMail().setProvider("log");
        mailDeliveryService = new MailDeliveryService(mailSenderPort, mailDeliveryLogRepository, appProperties);
    }

    @Test
    void sendPasswordResetShouldPersistSentLogWhenProviderSucceeds() {
        when(mailSenderPort.send(any(), any(), any())).thenReturn("delivery-id");

        mailDeliveryService.sendPasswordReset("fatih@example.com", "https://example.com/reset");

        ArgumentCaptor<MailDeliveryLog> captor = ArgumentCaptor.forClass(MailDeliveryLog.class);
        verify(mailDeliveryLogRepository).save(captor.capture());
        assertThat(captor.getValue().getPurpose()).isEqualTo(MailPurpose.PASSWORD_RESET);
        assertThat(captor.getValue().getStatus()).isEqualTo(MailDeliveryStatus.SENT);
        assertThat(captor.getValue().getProviderMessage()).isEqualTo("delivery-id");
    }

    @Test
    void sendContactMessageShouldPersistFailedLogWhenProviderFails() {
        doThrow(new RuntimeException("smtp failure")).when(mailSenderPort).send(any(), any(), any());

        mailDeliveryService.sendContactMessage("fatih@example.com", "Subject", "Body");

        ArgumentCaptor<MailDeliveryLog> captor = ArgumentCaptor.forClass(MailDeliveryLog.class);
        verify(mailDeliveryLogRepository).save(captor.capture());
        assertThat(captor.getValue().getPurpose()).isEqualTo(MailPurpose.CONTACT_MESSAGE);
        assertThat(captor.getValue().getStatus()).isEqualTo(MailDeliveryStatus.FAILED);
        assertThat(captor.getValue().getProviderMessage()).contains("smtp failure");
    }
}
