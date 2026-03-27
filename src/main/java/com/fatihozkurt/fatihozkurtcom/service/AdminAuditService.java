package com.fatihozkurt.fatihozkurtcom.service;

import com.fatihozkurt.fatihozkurtcom.domain.entity.AdminAuditEvent;
import com.fatihozkurt.fatihozkurtcom.domain.repository.AdminAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provides admin audit logging operations.
 */
@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditEventRepository adminAuditEventRepository;

    /**
     * Persists an audit event.
     *
     * @param actor actor
     * @param action action
     * @param resource resource
     * @param details details
     */
    public void log(String actor, String action, String resource, String details) {
        AdminAuditEvent event = new AdminAuditEvent();
        event.setActor(actor);
        event.setAction(action);
        event.setResource(resource);
        event.setDetails(details);
        adminAuditEventRepository.save(event);
    }
}
