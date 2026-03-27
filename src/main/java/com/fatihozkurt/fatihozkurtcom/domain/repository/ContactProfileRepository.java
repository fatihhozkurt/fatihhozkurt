package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.ContactProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for contact profile configuration.
 */
public interface ContactProfileRepository extends JpaRepository<ContactProfile, UUID> {
}
