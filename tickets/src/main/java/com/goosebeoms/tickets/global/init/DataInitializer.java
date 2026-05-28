package com.goosebeoms.tickets.global.init;

import com.goosebeoms.tickets.domain.coupon.entity.Coupon;
import com.goosebeoms.tickets.domain.coupon.repository.CouponRepository;
import com.goosebeoms.tickets.domain.show.entity.*;
import com.goosebeoms.tickets.domain.show.repository.*;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (showRepository.count() > 0) return;

        createUsers();

        createMusicals();
        createEdmFestivals();
        createOtherEvents();

        createCoupons();
    }

    private void createMusicals() {
        // ON_SALE: 예매 중
        createShow("레미제라블", "뮤지컬 레미제라블 내한 공연", "블루스퀘어 신한카드홀",
                Show.Category.MUSICAL, Show.Status.ON_SALE, 80000, 170000,
                "https://ticketimage.interpark.com/Play/image/large/23/23012526_p.gif",
                ldt("2026-05-01"), ldt("2026-07-31"));
        createShow("위키드", "전 세계가 사랑한 마법 뮤지컬", "샤롯데씨어터",
                Show.Category.MUSICAL, Show.Status.ON_SALE, 70000, 180000,
                "https://ticketimage.interpark.com/Play/image/large/25/25005777_p.gif",
                ldt("2026-04-15"), ldt("2026-06-30"));
        createShow("오페라의 유령", "앤드루 로이드 웨버 명작 25주년 기념", "블루스퀘어 신한카드홀",
                Show.Category.MUSICAL, Show.Status.ON_SALE, 90000, 190000,
                "https://ticketimage.interpark.com/Play/image/large/23/23000632_p.gif",
                ldt("2026-03-10"), ldt("2026-06-15"));
        createShow("시카고", "재즈 시대를 배경으로 한 뮤지컬 클래식", "LG아트센터",
                Show.Category.MUSICAL, Show.Status.ON_SALE, 60000, 150000,
                "https://ticketimage.interpark.com/Play/image/large/23/23004325_p.gif",
                ldt("2026-05-20"), ldt("2026-08-20"));
        createShow("맘마미아", "ABBA의 명곡과 함께하는 그리스 섬 이야기", "디큐브 링크아트센터",
                Show.Category.MUSICAL, Show.Status.ON_SALE, 70000, 160000,
                "https://ticketimage.interpark.com/Play/image/large/L0/L0000123_p.gif",
                ldt("2026-04-01"), ldt("2026-07-01"));
        createShow("라이언킹", "디즈니 뮤지컬 라이브", "예술의전당 오페라극장",
                Show.Category.MUSICAL, Show.Status.ON_SALE, 80000, 180000,
                "https://ticketimage.interpark.com/Play/image/large/21/21011619_p.gif",
                ldt("2026-02-01"), ldt("2026-06-01"));
        createShow("지킬 앤 하이드", "프랭크 와일드혼 명작", "블루스퀘어 신한카드홀",
                Show.Category.MUSICAL, Show.Status.ON_SALE, 60000, 140000,
                "https://ticketimage.interpark.com/Play/image/large/24/24013928_p.gif",
                ldt("2026-05-10"), ldt("2026-09-30"));
        // UPCOMING: 예매 예정
        createShow("베토벤", "최고의 배우들이 선보이는 베토벤", "세종문화회관 대극장",
                Show.Category.MUSICAL, Show.Status.UPCOMING, 60000, 140000,
                "https://ticketimage.interpark.com/Play/image/large/26/26006232_p.gif",
                ldt("2026-07-01"), ldt("2026-09-30"));
        createShow("겨울왕국", "디즈니 온 브로드웨이 첫 내한", "잠실 샤롯데씨어터",
                Show.Category.MUSICAL, Show.Status.UPCOMING, 90000, 200000,
                "https://ticketimage.interpark.com/Play/image/large/26/26007416_p.gif",
                ldt("2026-08-01"), ldt("2026-11-30"));
        createShow("빌리 엘리어트", "전세계가 사랑한 명작의 귀환", "블루스퀘어 우리은행홀",
                Show.Category.MUSICAL, Show.Status.UPCOMING, 100000, 220000,
                "https://ticketimage.interpark.com/Play/image/large/26/26001001_p.gif",
                ldt("2026-09-01"), ldt("2026-12-31"));
    }

    private void createEdmFestivals() {
        createShow("UMF Korea 2026", "Ultra Music Festival 한국", "잠실종합운동장",
                Show.Category.CONCERT, Show.Status.ON_SALE, 180000, 380000,
                "https://ticketimage.interpark.com/Play/image/large/25/25012744_p.gif",
                ldt("2026-04-20"), ldt("2026-06-20"));
        createShow("World DJ Festival", "글로벌 DJ 라인업", "서울랜드",
                Show.Category.CONCERT, Show.Status.ON_SALE, 130000, 280000,
                "https://ticketimage.interpark.com/Play/image/large/25/25011306_p.gif",
                ldt("2026-05-05"), ldt("2026-07-05"));
        createShow("서울 파크뮤직 페스티벌", "잔디밭에서 즐기는 음악", "올림픽공원 88잔디마당",
                Show.Category.CONCERT, Show.Status.ON_SALE, 140000, 290000,
                "https://ticketimage.interpark.com/Play/image/large/26/26003322_p.gif",
                ldt("2026-05-15"), ldt("2026-08-15"));
        createShow("Spectrum Dance Music Festival", "한강 야경과 함께하는 EDM 페스티벌", "난지한강공원",
                Show.Category.CONCERT, Show.Status.UPCOMING, 120000, 250000,
                "https://ticketimage.interpark.com/Play/image/large/16/16009350_p.gif",
                ldt("2026-07-10"), ldt("2026-09-10"));
    }

    private void createOtherEvents() {
        // 콘서트
        createShow("아이유 콘서트 2026", "아이유 월드투어 서울", "올림픽공원 KSPO돔",
                Show.Category.CONCERT, Show.Status.ON_SALE, 99000, 165000,
                "https://placehold.co/400x600?text=IU+Concert",
                ldt("2026-05-25"), ldt("2026-07-25"));
        createShow("NewJeans World Tour", "뉴진스 첫 단독 월드투어 서울", "고척스카이돔",
                Show.Category.CONCERT, Show.Status.ON_SALE, 120000, 220000,
                "https://placehold.co/400x600?text=NewJeans",
                ldt("2026-05-12"), ldt("2026-08-12"));
        createShow("임영웅 콘서트", "전국투어 IM HERO", "올림픽공원 KSPO돔",
                Show.Category.CONCERT, Show.Status.ON_SALE, 100000, 180000,
                "https://placehold.co/400x600?text=Lim+Young+Woong",
                ldt("2026-03-01"), ldt("2026-06-30"));
        createShow("프로농구 올스타전 2026", "KBL 올스타전 + 덩크 컨테스트", "잠실실내체육관",
                Show.Category.SPORTS, Show.Status.ON_SALE, 35000, 100000,
                "https://placehold.co/400x600?text=KBL+Allstar",
                ldt("2026-04-10"), ldt("2026-06-10"));
        // 연극
        createShow("햄릿", "셰익스피어 4대 비극", "명동예술극장",
                Show.Category.THEATER, Show.Status.ON_SALE, 40000, 90000,
                "https://placehold.co/400x600?text=Hamlet",
                ldt("2026-04-28"), ldt("2026-07-28"));
        createShow("라이어", "코미디 연극 시즌 9", "대학로 예술극장",
                Show.Category.THEATER, Show.Status.ON_SALE, 35000, 70000,
                "https://placehold.co/400x600?text=Liar",
                ldt("2026-05-08"), ldt("2026-08-08"));
        createShow("아가사", "추리극의 정석", "두산아트센터 연강홀",
                Show.Category.THEATER, Show.Status.ON_SALE, 45000, 95000,
                "https://placehold.co/400x600?text=Agatha",
                ldt("2026-02-14"), ldt("2026-06-14"));
        // 예매 예정
        createShow("BTS WORLD TOUR ‘ARIRANG’ IN BUSAN", "월드스타 BTS 컴백 콘서트", "부산아시아드 주경기장",
                Show.Category.CONCERT, Show.Status.UPCOMING, 220000, 264000,
                "https://ticketimage.interpark.com/Play/image/large/26/26005547_p.gif",
                ldt("2026-06-20"), ldt("2026-09-20"));
        createShow("노트르담 드 파리", "프랑스 오리지널 내한", "세종문화회관 대극장",
                Show.Category.THEATER, Show.Status.UPCOMING, 70000, 160000,
                "https://placehold.co/400x600?text=Notre+Dame+de+Paris",
                ldt("2026-07-15"), ldt("2026-10-15"));
        createShow("손흥민 축구 아카데미", "프리미어리그 레전드 특강", "서울월드컵경기장",
                Show.Category.SPORTS, Show.Status.UPCOMING, 30000, 80000,
                "https://placehold.co/400x600?text=Son+Football",
                ldt("2026-06-01"), ldt("2026-08-31"));
        createShow("KBO 한국시리즈 1차전", "프로야구 가을야구의 절정", "잠실야구장",
                Show.Category.SPORTS, Show.Status.UPCOMING, 25000, 90000,
                "https://placehold.co/400x600?text=KBO+Korean+Series",
                ldt("2026-09-01"), ldt("2026-11-01"));
    }

    private void createUsers() {
        userRepository.save(User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("테스트유저")
                .phone("010-1234-5678")
                .role(User.Role.USER)
                .build());

        userRepository.save(User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("관리자")
                .phone("010-0000-0000")
                .role(User.Role.ADMIN)
                .build());
    }

    private void createCoupons() {
        LocalDateTime now = LocalDateTime.now();

        couponRepository.save(Coupon.builder()
                .code("WELCOME10")
                .name("신규 가입 10% 할인")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(10)
                .maxCount(100)
                .validFrom(now.minusDays(1))
                .validUntil(now.plusDays(30))
                .build());

        couponRepository.save(Coupon.builder()
                .code("EARLY5000")
                .name("선착순 5,000원 할인")
                .discountType(Coupon.DiscountType.FIXED)
                .discountValue(5000)
                .maxCount(50)
                .validFrom(now.minusDays(1))
                .validUntil(now.plusDays(7))
                .build());

        couponRepository.save(Coupon.builder()
                .code("VIP20")
                .name("VIP 20% 할인")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(20)
                .maxCount(10)
                .validFrom(now.minusDays(1))
                .validUntil(now.plusDays(14))
                .build());
    }

    private static LocalDateTime ldt(String date) {
        return LocalDateTime.parse(date + "T00:00:00");
    }

    private void createShow(String title, String description, String venue,
                            Show.Category category, Show.Status status,
                            int minPrice, int maxPrice, String posterUrl,
                            LocalDateTime bookingStartAt, LocalDateTime bookingEndAt) {
        Show show = showRepository.save(Show.builder()
                .title(title)
                .description(description)
                .venue(venue)
                .category(category)
                .status(status)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .posterUrl(posterUrl)
                .bookingStartAt(bookingStartAt)
                .bookingEndAt(bookingEndAt)
                .build());

        for (int i = 1; i <= 2; i++) {
            ShowSchedule schedule = scheduleRepository.save(ShowSchedule.builder()
                    .show(show)
                    .scheduledAt(LocalDateTime.now().plusDays(i * 7L))
                    .totalCapacity(332)
                    .build());

            createZoneWithSeats(schedule, "VIP", maxPrice, 3, 12);
            createZoneWithSeats(schedule, "R", (minPrice + maxPrice) / 2, 6, 16);
            createZoneWithSeats(schedule, "S", minPrice, 10, 20);
        }
    }

    private void createZoneWithSeats(ShowSchedule schedule, String zoneName, int price, int rows, int cols) {
        if (rows > 26) {
            throw new IllegalArgumentException("rowLabel A~Z 범위 초과: " + rows);
        }
        Zone zone = zoneRepository.save(Zone.builder()
                .showSchedule(schedule)
                .name(zoneName)
                .price(price)
                .rowCount(rows)
                .columnCount(cols)
                .build());

        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            String rowLabel = String.valueOf((char) ('A' + r));
            for (int c = 1; c <= cols; c++) {
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
