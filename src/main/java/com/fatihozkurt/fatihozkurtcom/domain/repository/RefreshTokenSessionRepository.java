package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminUser;
import com.fatihozkurt.fatihozkurtcom.domain.entity.RefreshTokenSession;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for refresh token sessions.
 */
public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, UUID> {
    Optional<RefreshTokenSession> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    @Modifying
    @Query("update RefreshTokenSession r set r.revokedAt = :revokedAt where r.adminUser = :adminUser and r.revokedAt is null")
    int revokeAllByAdminUser(AdminUser adminUser, OffsetDateTime revokedAt);
}
