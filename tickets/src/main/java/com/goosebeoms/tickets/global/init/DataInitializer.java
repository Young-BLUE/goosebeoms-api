package com.goosebeoms.tickets.global.init;

import com.goosebeoms.tickets.domain.show.entity.*;
import com.goosebeoms.tickets.domain.show.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ShowRepository showRepository;
    private final ShowScheduleRepository scheduleRepository;
    private final ZoneRepository zoneRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (showRepository.count() > 0) return;

        createShow("레미제라블", "뮤지컬 레미제라블 내한 공연", "블루스퀘어 신한카드홀",
                Show.Category.MUSICAL, Show.Status.ON_SALE, 80000, 170000,
                "https://placehold.co/400x600?text=LesMis");

        createShow("아이유 콘서트 2026", "아이유 월드투어 서울", "올림픽공원 케이스포돔",
                Show.Category.CONCERT, Show.Status.ON_SALE, 99000, 165000,
                "https://placehold.co/400x600?text=IU");

        createShow("손흥민 축구 아카데미", "프리미어리그 레전드 특강", "서울월드컵경기장",
                Show.Category.SPORTS, Show.Status.UPCOMING, 30000, 80000,
                "https://placehold.co/400x600?text=Soccer");
    }

    private void createShow(String title, String description, String venue,
                            Show.Category category, Show.Status status,
                            int minPrice, int maxPrice, String posterUrl) {
        Show show = showRepository.save(Show.builder()
                .title(title)
                .description(description)
                .venue(venue)
                .category(category)
                .status(status)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .posterUrl(posterUrl)
                .build());

        for (int i = 1; i <= 2; i++) {
            ShowSchedule schedule = scheduleRepository.save(ShowSchedule.builder()
                    .show(show)
                    .scheduledAt(LocalDateTime.now().plusDays(i * 7L))
                    .totalCapacity(200)
                    .build());

            createZoneWithSeats(schedule, "VIP", maxPrice, 2, 10);
            createZoneWithSeats(schedule, "R", (minPrice + maxPrice) / 2, 5, 10);
            createZoneWithSeats(schedule, "S", minPrice, 8, 10);
        }
    }

    private void createZoneWithSeats(ShowSchedule schedule, String zoneName, int price, int rows, int cols) {
        Zone zone = zoneRepository.save(Zone.builder()
                .showSchedule(schedule)
                .name(zoneName)
                .price(price)
                .rowCount(rows)
                .columnCount(cols)
                .build());

        String[] rowLabels = {"A", "B", "C", "D", "E", "F", "G", "H"};
        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 1; c <= cols; c++) {
                seats.add(Seat.builder()
                        .zone(zone)
                        .rowLabel(rowLabels[r])
                        .number(c)
                        .build());
            }
        }
        seatRepository.saveAll(seats);
    }
}
