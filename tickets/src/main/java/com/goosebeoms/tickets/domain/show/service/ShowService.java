package com.goosebeoms.tickets.domain.show.service;

import com.goosebeoms.tickets.domain.show.dto.*;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.Show;
import com.goosebeoms.tickets.domain.show.entity.Zone;
import com.goosebeoms.tickets.domain.show.repository.*;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowScheduleRepository scheduleRepository;
    private final ZoneRepository zoneRepository;
    private final SeatRepository seatRepository;

    public Page<ShowResponse> getShows(ShowSearchCondition cond, Pageable pageable) {
        String q = StringUtils.hasText(cond.q()) ? cond.q().trim() : null;
        return showRepository.search(
                q,
                cond.category(),
                cond.status(),
                cond.minPrice(),
                cond.maxPrice(),
                cond.dateFrom(),
                cond.dateTo(),
                pageable
        ).map(ShowResponse::from);
    }

    public record ShowSearchCondition(
            String q,
            Show.Category category,
            Show.Status status,
            Integer minPrice,
            Integer maxPrice,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {}

    public ShowDetailResponse getShow(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));

        List<ShowScheduleResponse> schedules = scheduleRepository.findByShowId(showId).stream()
                .map(ShowScheduleResponse::from)
                .toList();

        return ShowDetailResponse.from(show, schedules);
    }

    public List<ShowScheduleResponse> getSchedules(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new BusinessException(ErrorCode.SHOW_NOT_FOUND);
        }
        return scheduleRepository.findByShowId(showId).stream()
                .map(ShowScheduleResponse::from)
                .toList();
    }

    public List<ZoneResponse> getZones(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        List<Zone> zones = zoneRepository.findByShowScheduleId(scheduleId);
        List<Long> zoneIds = zones.stream().map(Zone::getId).toList();
        List<Seat> seats = seatRepository.findByZoneIdIn(zoneIds);

        return zones.stream()
                .map(zone -> {
                    long available = seats.stream()
                            .filter(s -> s.getZone().getId().equals(zone.getId()))
                            .filter(s -> s.getStatus() == Seat.SeatStatus.AVAILABLE)
                            .count();
                    return ZoneResponse.from(zone, available);
                })
                .toList();
    }

    public List<SeatResponse> getSeats(Long scheduleId, Long zoneId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        List<Seat> seats = zoneId != null
                ? seatRepository.findByZoneId(zoneId)
                : seatRepository.findByShowScheduleId(scheduleId);

        return seats.stream().map(SeatResponse::from).toList();
    }
}
