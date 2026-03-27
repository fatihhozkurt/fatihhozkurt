package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Provides common identifier and audit timestamps for entities.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Initializes id and audit fields before insert.
     */
    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Updates audit timestamp before update.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
