package com.fatihozkurt.fatihozkurtcom.service.mail;

/**
 * Sends outbound email messages.
 */
public interface MailSenderPort {

    /**
     * Sends mail with subject and content.
     *
     * @param recipient recipient
     * @param subject subject
     * @param htmlBody html content
     * @return provider response message
     */
    String send(String recipient, String subject, String htmlBody);
}
