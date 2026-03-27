package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEvent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEventType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for security events.
 */
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {
    long countByOccurredAtAfter(OffsetDateTime occurredAt);
    long countByEventTypeAndOccurredAtAfter(SecurityEventType eventType, OffsetDateTime occurredAt);
    List<SecurityEvent> findTop50ByOrderByOccurredAtDesc();
}
