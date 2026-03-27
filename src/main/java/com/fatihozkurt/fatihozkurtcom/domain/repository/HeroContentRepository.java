package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.HeroContent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for hero content.
 */
public interface HeroContentRepository extends JpaRepository<HeroContent, UUID> {
}
