package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores public visit telemetry events.
 */
@Getter
@Setter
@Entity
@Table(name = "visit_events")
public class VisitEvent extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String path;

    @Column(length = 80)
    private String country;

    @Column(length = 80)
    private String ipAddress;

    @Column(length = 300)
    private String userAgent;

    @Column(nullable = false)
    private OffsetDateTime occurredAt = OffsetDateTime.now();
}
