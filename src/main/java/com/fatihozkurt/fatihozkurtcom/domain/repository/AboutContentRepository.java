package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.AboutContent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for about content.
 */
public interface AboutContentRepository extends JpaRepository<AboutContent, UUID> {
}
