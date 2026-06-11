package com.goosebeoms.tickets.domain.show.service;

import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.show.dto.AdminScheduleCreateRequest;
import com.goosebeoms.tickets.domain.show.dto.AdminScheduleResponse;
import com.goosebeoms.tickets.domain.show.dto.AdminScheduleUpdateRequest;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.Show;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.entity.Zone;
import com.goosebeoms.tickets.domain.show.repository.SeatRepository;
import com.goosebeoms.tickets.domain.show.repository.ShowRepository;
import com.goosebeoms.tickets.domain.show.repository.ShowScheduleRepository;
import com.goosebeoms.tickets.domain.show.repository.ZoneRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminScheduleService {

    private final ShowRepository showRepository;
    private final ShowScheduleRepository scheduleRepository;
    private final ZoneRepository zoneRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;

    public AdminScheduleResponse create(AdminScheduleCreateRequest request) {
        Show show = showRepository.findById(request.showId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));

        Set<String> zoneNames = new HashSet<>();
        for (AdminScheduleCreateRequest.ZoneSpec z : request.zones()) {
            if (!zoneNames.add(z.name())) {
                throw new BusinessException(ErrorCode.ZONE_NAME_DUPLICATED);
            }
        }

        int totalCapacity = request.zones().stream()
                .mapToInt(AdminScheduleCreateRequest.ZoneSpec::seatCount)
                .sum();

        ShowSchedule schedule = scheduleRepository.save(ShowSchedule.builder()
                .show(show)
                .scheduledAt(request.scheduledAt())
                .totalCapacity(totalCapacity)
                .build());

        List<Zone> zones = new ArrayList<>();
        for (AdminScheduleCreateRequest.ZoneSpec spec : request.zones()) {
            Zone zone = zoneRepository.save(Zone.builder()
                    .showSchedule(schedule)
                    .name(spec.name())
                    .price(spec.price())
                    .rowCount(spec.rowCount())
                    .columnCount(spec.columnCount())
                    .build());
            zones.add(zone);
            seedSeats(zone);
        }

        List<Long> availableCounts = zones.stream()
                .map(z -> (long) (z.getRowCount() * z.getColumnCount()))
                .toList();
        return AdminScheduleResponse.from(schedule, zones, availableCounts);
    }

    public AdminScheduleResponse update(Long scheduleId, AdminScheduleUpdateRequest request) {
        ShowSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (request.scheduledAt() != null) {
            schedule.changeScheduledAt(request.scheduledAt());
        }
        return loadResponse(schedule);
    }

    public void delete(Long scheduleId) {
        ShowSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (bookingRepository.countByShowScheduleId(scheduleId) > 0) {
            throw new BusinessException(ErrorCode.SCHEDULE_HAS_BOOKINGS);
        }
        List<Zone> zones = zoneRepository.findByShowScheduleId(scheduleId);
        List<Long> zoneIds = zones.stream().map(Zone::getId).toList();
        if (!zoneIds.isEmpty()) {
            seatRepository.deleteAll(seatRepository.findByZoneIdIn(zoneIds));
            zoneRepository.deleteAll(zones);
        }
        scheduleRepository.delete(schedule);
    }

    @Transactional(readOnly = true)
    public List<AdminScheduleResponse> listByShow(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new BusinessException(ErrorCode.SHOW_NOT_FOUND);
        }
        return scheduleRepository.findByShowId(showId).stream()
                .map(this::loadResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminScheduleResponse get(Long scheduleId) {
        ShowSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        return loadResponse(schedule);
    }

    private AdminScheduleResponse loadResponse(ShowSchedule schedule) {
        List<Zone> zones = zoneRepository.findByShowScheduleId(schedule.getId());
        if (zones.isEmpty()) {
            return AdminScheduleResponse.from(schedule, zones, List.of());
        }
        List<Long> zoneIds = zones.stream().map(Zone::getId).toList();
        Map<Long, Long> availableByZone = seatRepository.countAvailableByZoneIds(zoneIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        List<Long> availableCounts = zones.stream()
                .map(z -> availableByZone.getOrDefault(z.getId(), 0L))
                .toList();
        return AdminScheduleResponse.from(schedule, zones, availableCounts);
    }

    private void seedSeats(Zone zone) {
        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < zone.getRowCount(); r++) {
            String rowLabel = String.valueOf((char) ('A' + r));
            for (int c = 1; c <= zone.getColumnCount(); c++) {
                seats.add(Seat.builder()
                        .zone(zone)
                        .rowLabel(rowLabel)
                        .number(c)
                        .build());
            }
        }
        seatRepository.saveAll(seats);
    }
}
