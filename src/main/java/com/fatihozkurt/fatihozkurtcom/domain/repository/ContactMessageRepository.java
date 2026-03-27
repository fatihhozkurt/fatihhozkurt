package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactMessage;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for contact form messages.
 */
public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {
    long countByCreatedAtAfter(OffsetDateTime createdAt);
}
