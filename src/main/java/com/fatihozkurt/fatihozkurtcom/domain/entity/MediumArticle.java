package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores Medium article card metadata.
 */
@Getter
@Setter
@Entity
@Table(name = "medium_articles")
public class MediumArticle extends BaseEntity {

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 1500)
    private String excerpt;

    @Column(nullable = false, length = 320)
    private String href;

    @Column(length = 80)
    private String readingTime;

    @Column
    private LocalDate publishedAt;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active = true;
}
