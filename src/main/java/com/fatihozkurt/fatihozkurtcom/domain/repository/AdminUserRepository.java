package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for admin users.
 */
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {
    Optional<AdminUser> findByUsernameIgnoreCase(String username);
    Optional<AdminUser> findByEmailIgnoreCase(String email);
}
