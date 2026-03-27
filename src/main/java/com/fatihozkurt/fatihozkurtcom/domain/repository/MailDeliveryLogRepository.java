package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.MailDeliveryLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for mail delivery logs.
 */
public interface MailDeliveryLogRepository extends JpaRepository<MailDeliveryLog, UUID> {
    List<MailDeliveryLog> findTop100ByOrderByAttemptedAtDesc();
}
