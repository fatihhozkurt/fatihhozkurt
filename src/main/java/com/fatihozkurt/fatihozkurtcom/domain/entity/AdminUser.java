package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores admin account credentials and access role.
 */
@Getter
@Setter
@Entity
@Table(name = "admin_users")
public class AdminUser extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AdminRole role = AdminRole.ROLE_ADMIN;

    @Column(nullable = false)
    private boolean active = true;
}
