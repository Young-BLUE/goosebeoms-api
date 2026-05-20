package com.goosebeoms.tickets.domain.booking.repository;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
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
}
