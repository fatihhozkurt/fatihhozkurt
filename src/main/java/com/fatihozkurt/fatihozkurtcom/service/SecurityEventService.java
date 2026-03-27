package com.fatihozkurt.fatihozkurtcom.service;

import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEvent;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecurityEventType;
import com.fatihozkurt.fatihozkurtcom.domain.entity.SecuritySeverity;
import com.fatihozkurt.fatihozkurtcom.domain.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provides security event logging operations.
 */
@Service
@RequiredArgsConstructor
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;

    /**
     * Persists a security event.
     *
     * @param type type
     * @param severity severity
     * @param username username
     * @param ipAddress ip
     * @param details details
     */
    public void log(SecurityEventType type, SecuritySeverity severity, String username, String ipAddress, String details) {
        SecurityEvent event = new SecurityEvent();
        event.setEventType(type);
        event.setSeverity(severity);
        event.setUsername(username);
        event.setIpAddress(ipAddress);
        event.setDetails(details);
        securityEventRepository.save(event);
    }
}
