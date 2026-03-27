package com.fatihozkurt.fatihozkurtcom.service.mail;

import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import com.fatihozkurt.fatihozkurtcom.domain.entity.MailDeliveryLog;
import com.fatihozkurt.fatihozkurtcom.domain.entity.MailDeliveryStatus;
import com.fatihozkurt.fatihozkurtcom.domain.entity.MailPurpose;
import com.fatihozkurt.fatihozkurtcom.domain.repository.MailDeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Coordinates outbound mail sends and delivery logs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailDeliveryService {

    private final MailSenderPort mailSenderPort;
    private final MailDeliveryLogRepository mailDeliveryLogRepository;
    private final AppProperties appProperties;

    /**
     * Sends and logs password reset email.
     *
     * @param recipient recipient email
     * @param resetLink reset link
     */
    public void sendPasswordReset(String recipient, String resetLink) {
        String subject = "Password reset request";
        String html = "<h3>Password reset</h3><p>Use this link to reset your password:</p><p><a href=\"" + resetLink + "\">Reset password</a></p>";
        deliver(MailPurpose.PASSWORD_RESET, recipient, subject, html);
    }

    /**
     * Sends and logs contact message email.
     *
     * @param recipient recipient email
     * @param subject subject
     * @param body body
     */
    public void sendContactMessage(String recipient, String subject, String body) {
        String html = "<h3>Contact form message</h3><p>" + body + "</p>";
        deliver(MailPurpose.CONTACT_MESSAGE, recipient, subject, html);
    }

    private void deliver(MailPurpose purpose, String recipient, String subject, String html) {
        MailDeliveryLog logEntry = new MailDeliveryLog();
        logEntry.setPurpose(purpose);
        logEntry.setRecipient(recipient);
        logEntry.setProvider(appProperties.getMail().getProvider());
        try {
            String providerMessage = mailSenderPort.send(recipient, subject, html);
            logEntry.setStatus(MailDeliveryStatus.SENT);
            logEntry.setProviderMessage(providerMessage);
            log.info("Mail delivered purpose={} recipient={}", purpose, recipient);
        } catch (Exception ex) {
            logEntry.setStatus(MailDeliveryStatus.FAILED);
            logEntry.setProviderMessage(ex.getMessage());
            log.warn("Mail delivery failed purpose={} recipient={} reason={}", purpose, recipient, ex.getMessage());
        }
        mailDeliveryLogRepository.save(logEntry);
    }
}
