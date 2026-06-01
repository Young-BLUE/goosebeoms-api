package com.goosebeoms.tickets.domain.show.repository;

import com.goosebeoms.tickets.domain.show.entity.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ShowRepository extends JpaRepository<Show, Long> {

    long countByStatus(Show.Status status);


    @Query(value = """
            SELECT DISTINCT s FROM Show s
            WHERE (:q IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:category IS NULL OR s.category = :category)
              AND (:status IS NULL OR s.status = :status)
              AND (:minPrice IS NULL OR s.maxPrice >= :minPrice)
              AND (:maxPrice IS NULL OR s.minPrice <= :maxPrice)
              AND (:dateFrom IS NULL OR EXISTS (
                  SELECT 1 FROM ShowSchedule sc WHERE sc.show = s AND sc.scheduledAt >= :dateFrom))
              AND (:dateTo IS NULL OR EXISTS (
                  SELECT 1 FROM ShowSchedule sc WHERE sc.show = s AND sc.scheduledAt <= :dateTo))
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s) FROM Show s
            WHERE (:q IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:category IS NULL OR s.category = :category)
              AND (:status IS NULL OR s.status = :status)
              AND (:minPrice IS NULL OR s.maxPrice >= :minPrice)
              AND (:maxPrice IS NULL OR s.minPrice <= :maxPrice)
              AND (:dateFrom IS NULL OR EXISTS (
                  SELECT 1 FROM ShowSchedule sc WHERE sc.show = s AND sc.scheduledAt >= :dateFrom))
              AND (:dateTo IS NULL OR EXISTS (
                  SELECT 1 FROM ShowSchedule sc WHERE sc.show = s AND sc.scheduledAt <= :dateTo))
            """)
    Page<Show> search(
            @Param("q") String q,
            @Param("category") Show.Category category,
            @Param("status") Show.Status status,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}
