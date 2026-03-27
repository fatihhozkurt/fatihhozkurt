package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores public hero section content.
 */
@Getter
@Setter
@Entity
@Table(name = "hero_content")
public class HeroContent extends BaseEntity {

    @Column(nullable = false, length = 180)
    private String welcomeText;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1200)
    private String description;

    @Column(nullable = false, length = 80)
    private String ctaLabel;
}
