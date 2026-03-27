package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores mail delivery attempt records.
 */
@Getter
@Setter
@Entity
@Table(name = "mail_delivery_logs")
public class MailDeliveryLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private MailPurpose purpose;

    @Column(nullable = false, length = 180)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MailDeliveryStatus status;

    @Column(length = 80)
    private String provider;

    @Column(length = 1000)
    private String providerMessage;

    @Column(nullable = false)
    private OffsetDateTime attemptedAt = OffsetDateTime.now();
}
