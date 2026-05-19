package com.goosebeoms.tickets.domain.show.repository;

import com.goosebeoms.tickets.domain.show.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findByShowScheduleId(Long showScheduleId);
}
