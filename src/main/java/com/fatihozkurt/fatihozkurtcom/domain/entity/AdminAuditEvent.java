package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores auditable admin operations.
 */
@Getter
@Setter
@Entity
@Table(name = "admin_audit_events")
public class AdminAuditEvent extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String actor;

    @Column(nullable = false, length = 180)
    private String action;

    @Column(nullable = false, length = 120)
    private String resource;

    @Column(length = 800)
    private String details;

    @Column(nullable = false)
    private OffsetDateTime occurredAt = OffsetDateTime.now();
}
