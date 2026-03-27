package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores public about section content.
 */
@Getter
@Setter
@Entity
@Table(name = "about_content")
public class AboutContent extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String eyebrow;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 1200)
    private String description;
}
