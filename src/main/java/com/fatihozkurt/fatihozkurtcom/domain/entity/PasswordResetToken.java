package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores single-use password reset tokens.
 */
@Getter
@Setter
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private AdminUser adminUser;

    @Column(nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column
    private OffsetDateTime usedAt;
}
