package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores public tech stack item configuration.
 */
@Getter
@Setter
@Entity
@Table(name = "tech_stack_items")
public class TechStackItem extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String iconName;

    @Column(length = 80)
    private String category;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active = true;
}
