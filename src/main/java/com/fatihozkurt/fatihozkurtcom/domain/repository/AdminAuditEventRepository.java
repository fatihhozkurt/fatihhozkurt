package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminAuditEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for admin audit events.
 */
public interface AdminAuditEventRepository extends JpaRepository<AdminAuditEvent, UUID> {
    List<AdminAuditEvent> findTop100ByOrderByOccurredAtDesc();
}
