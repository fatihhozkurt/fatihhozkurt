package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.CvDocument;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for CV document metadata.
 */
public interface CvDocumentRepository extends JpaRepository<CvDocument, UUID> {
    Optional<CvDocument> findFirstByActiveTrueOrderByUpdatedAtDesc();
}
