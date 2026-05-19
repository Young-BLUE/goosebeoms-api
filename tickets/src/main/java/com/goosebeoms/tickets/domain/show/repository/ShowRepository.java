package com.goosebeoms.tickets.domain.show.repository;

import com.goosebeoms.tickets.domain.show.entity.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {
    Page<Show> findByCategory(Show.Category category, Pageable pageable);
    Page<Show> findByStatus(Show.Status status, Pageable pageable);
    Page<Show> findByCategoryAndStatus(Show.Category category, Show.Status status, Pageable pageable);
}
