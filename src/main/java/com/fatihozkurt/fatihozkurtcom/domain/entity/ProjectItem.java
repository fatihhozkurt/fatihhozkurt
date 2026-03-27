package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores project card and detail metadata.
 */
@Getter
@Setter
@Entity
@Table(name = "project_items")
public class ProjectItem extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 1500)
    private String summary;

    @Column(length = 320)
    private String repositoryUrl;

    @Column(length = 320)
    private String demoUrl;

    @Column(length = 10000)
    private String readmeMarkdown;

    @Column(length = 255)
    private String coverImageUrl;

    @Column(length = 1200)
    private String stackCsv;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active = true;
}
