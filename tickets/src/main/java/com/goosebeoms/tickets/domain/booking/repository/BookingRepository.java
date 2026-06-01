package com.goosebeoms.tickets.domain.booking.repository;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.bookingSeats bs JOIN FETCH bs.seat WHERE b.id = :id")
    Optional<Booking> findByIdWithSeats(@Param("id") Long id);

    @Query("SELECT b.id FROM Booking b WHERE b.status = 'HOLD' AND b.holdExpiresAt < :now")
    List<Long> findExpiredHoldIds(@Param("now") LocalDateTime now);

    long countByShowScheduleId(Long scheduleId);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(b.finalPrice), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    long sumConfirmedRevenue();

    @Query("SELECT COALESCE(SUM(b.finalPrice), 0) FROM Booking b " +
            "WHERE b.status = 'CONFIRMED' AND b.paidAt BETWEEN :from AND :to")
    long sumConfirmedRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT b FROM Booking b
            WHERE (:userId IS NULL OR b.user.id = :userId)
              AND (:scheduleId IS NULL OR b.showSchedule.id = :scheduleId)
              AND (:status IS NULL OR b.status = :status)
              AND (:from IS NULL OR b.createdAt >= :from)
              AND (:to IS NULL OR b.createdAt <= :to)
            """)
    Page<Booking> searchForAdmin(
            @Param("userId") Long userId,
            @Param("scheduleId") Long scheduleId,
            @Param("status") Booking.BookingStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
