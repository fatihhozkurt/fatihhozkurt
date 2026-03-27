package com.fatihozkurt.fatihozkurtcom.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Logs outbound mail payload for integration-ready local behavior.
 */
@Slf4j
@Component
public class LoggingMailSenderAdapter implements MailSenderPort {

    /**
     * Logs message and returns synthetic provider id.
     *
     * @param recipient recipient
     * @param subject subject
     * @param htmlBody body
     * @return synthetic delivery id
     */
    @Override
    public String send(String recipient, String subject, String htmlBody) {
        log.info("Mail delivery simulated recipient={} subject={} bodyLength={}", recipient, subject, htmlBody.length());
        return "log-delivery-ok";
    }
}
