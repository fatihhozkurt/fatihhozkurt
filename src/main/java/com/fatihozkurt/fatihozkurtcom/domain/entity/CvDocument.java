package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores active CV document metadata.
 */
@Getter
@Setter
@Entity
@Table(name = "cv_documents")
public class CvDocument extends BaseEntity {

    @Column(nullable = false, length = 180)
    private String fileName;

    @Column(nullable = false, length = 320)
    private String objectKey;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column
    private OffsetDateTime replacedAt;

    @Column(nullable = false)
    private boolean active = true;
}
