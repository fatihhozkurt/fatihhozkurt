package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.TechStackItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for tech stack items.
 */
public interface TechStackItemRepository extends JpaRepository<TechStackItem, UUID> {
    List<TechStackItem> findByActiveTrueOrderBySortOrderAscNameAsc();
    List<TechStackItem> findAllByOrderBySortOrderAscNameAsc();
}
