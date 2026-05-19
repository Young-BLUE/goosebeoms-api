package com.goosebeoms.tickets.domain.show.repository;

import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShowScheduleRepository extends JpaRepository<ShowSchedule, Long> {
    List<ShowSchedule> findByShowId(Long showId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM ShowSchedule s WHERE s.id = :id")
    Optional<ShowSchedule> findByIdWithOptimisticLock(@Param("id") Long id);
}
