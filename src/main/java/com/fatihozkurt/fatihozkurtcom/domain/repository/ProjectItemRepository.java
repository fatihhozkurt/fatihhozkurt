package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.ProjectItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for projects.
 */
public interface ProjectItemRepository extends JpaRepository<ProjectItem, UUID> {
    List<ProjectItem> findByActiveTrueOrderBySortOrderAscTitleAsc();
    List<ProjectItem> findAllByOrderBySortOrderAscTitleAsc();
}
