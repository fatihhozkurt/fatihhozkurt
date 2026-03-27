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
 * Stores security and authentication related events.
 */
@Getter
@Setter
@Entity
@Table(name = "security_events")
public class SecurityEvent extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private SecurityEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SecuritySeverity severity = SecuritySeverity.INFO;

    @Column(length = 100)
    private String username;

    @Column(length = 80)
    private String ipAddress;

    @Column(length = 600)
    private String details;

    @Column(nullable = false)
    private OffsetDateTime occurredAt = OffsetDateTime.now();
}
