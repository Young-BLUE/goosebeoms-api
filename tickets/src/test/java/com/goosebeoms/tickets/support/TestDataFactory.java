package com.goosebeoms.tickets.support;

import com.goosebeoms.tickets.domain.coupon.entity.Coupon;
import com.goosebeoms.tickets.domain.coupon.repository.CouponRepository;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.Show;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.entity.Zone;
import com.goosebeoms.tickets.domain.show.repository.SeatRepository;
import com.goosebeoms.tickets.domain.show.repository.ShowRepository;
import com.goosebeoms.tickets.domain.show.repository.ShowScheduleRepository;
import com.goosebeoms.tickets.domain.show.repository.ZoneRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataFactory {

    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final ShowScheduleRepository scheduleRepository;
    private final ZoneRepository zoneRepository;
    private final SeatRepository seatRepository;
    private final CouponRepository couponRepository;

    public User newUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password("encoded")
                .name(email.split("@")[0])
                .role(User.Role.USER)
                .build());
    }

    public List<User> newUsers(int count, String prefix) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(newUser(prefix + i + "@test.com"));
        }
        return users;
    }

    public ShowSchedule newSchedule(int seatCount) {
        Show show = showRepository.save(Show.builder()
                .title("Test Show")
                .description("desc")
                .venue("Venue")
                .category(Show.Category.MUSICAL)
                .status(Show.Status.ON_SALE)
                .minPrice(10000)
                .maxPrice(20000)
                .build());

        ShowSchedule schedule = scheduleRepository.save(ShowSchedule.builder()
                .show(show)
                .scheduledAt(LocalDateTime.now().plusDays(30))
                .totalCapacity(seatCount)
                .build());

        Zone zone = zoneRepository.save(Zone.builder()
                .showSchedule(schedule)
                .name("R")
                .price(15000)
                .rowCount(1)
                .columnCount(seatCount)
                .build());

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= seatCount; i++) {
            seats.add(Seat.builder()
                    .zone(zone)
                    .rowLabel("A")
                    .number(i)
                    .build());
        }
        seatRepository.saveAll(seats);
        return schedule;
    }

    public List<Seat> seatsOf(ShowSchedule schedule) {
        return seatRepository.findByShowScheduleId(schedule.getId());
    }

    public Coupon newCoupon(String code, int maxCount) {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.save(Coupon.builder()
                .code(code)
                .name("Test " + code)
                .discountType(Coupon.DiscountType.FIXED)
                .discountValue(1000)
                .maxCount(maxCount)
                .validFrom(now.minusDays(1))
                .validUntil(now.plusDays(7))
                .build());
    }
}
