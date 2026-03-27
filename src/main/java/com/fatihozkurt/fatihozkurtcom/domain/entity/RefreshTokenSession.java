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
 * Persists rotating refresh token sessions.
 */
@Getter
@Setter
@Entity
@Table(name = "refresh_token_sessions")
public class RefreshTokenSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private AdminUser adminUser;

    @Column(nullable = false, length = 128, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column
    private OffsetDateTime revokedAt;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 80)
    private String ipAddress;
}
