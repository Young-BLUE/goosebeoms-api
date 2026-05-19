package com.goosebeoms.tickets.domain.show.repository;

import com.goosebeoms.tickets.domain.show.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByZoneId(Long zoneId);
    List<Seat> findByZoneIdIn(List<Long> zoneIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :ids")
    List<Seat> findByIdsWithPessimisticLock(@Param("ids") List<Long> ids);

    @Query("SELECT s FROM Seat s WHERE s.zone.showSchedule.id = :scheduleId")
    List<Seat> findByShowScheduleId(@Param("scheduleId") Long scheduleId);
}
