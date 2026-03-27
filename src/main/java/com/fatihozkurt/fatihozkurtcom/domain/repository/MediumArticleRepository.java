package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.MediumArticle;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Medium article metadata.
 */
public interface MediumArticleRepository extends JpaRepository<MediumArticle, UUID> {
    List<MediumArticle> findByActiveTrueOrderBySortOrderAscTitleAsc();
    List<MediumArticle> findAllByOrderBySortOrderAscTitleAsc();
}
