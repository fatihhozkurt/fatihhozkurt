package com.fatihozkurt.fatihozkurtcom.domain.repository;

import com.fatihozkurt.fatihozkurtcom.domain.entity.VisitEvent;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for public visit events.
 */
public interface VisitEventRepository extends JpaRepository<VisitEvent, UUID> {
    long countByOccurredAtAfter(OffsetDateTime occurredAt);
    List<VisitEvent> findByOccurredAtAfter(OffsetDateTime occurredAt);

    @Query("select v.country, count(v) from VisitEvent v where v.occurredAt >= :after and v.country is not null group by v.country order by count(v) desc")
    List<Object[]> countCountries(OffsetDateTime after);

    @Query("select v.path, count(v) from VisitEvent v where v.occurredAt >= :after group by v.path order by count(v) desc")
    List<Object[]> countPaths(OffsetDateTime after);
}
