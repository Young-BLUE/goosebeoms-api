package com.goosebeoms.tickets.domain.coupon;

import com.goosebeoms.tickets.domain.coupon.entity.Coupon;
import com.goosebeoms.tickets.domain.coupon.repository.CouponRepository;
import com.goosebeoms.tickets.domain.coupon.repository.UserCouponRepository;
import com.goosebeoms.tickets.domain.coupon.service.CouponService;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CouponConcurrencyTest extends AbstractIntegrationTest {

    @Autowired CouponService couponService;
    @Autowired CouponRepository couponRepository;
    @Autowired UserCouponRepository userCouponRepository;
    @Autowired TestDataFactory factory;

    @Test
    void issuedCountNeverExceedsMaxCount() throws InterruptedException {
        int maxCount = 10;
        int requesters = 100;

        Coupon coupon = factory.newCoupon("CONCUR10", maxCount);
        List<User> users = factory.newUsers(requesters, "claimer");

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(requesters);
        ExecutorService pool = Executors.newFixedThreadPool(32);

        for (int i = 0; i < requesters; i++) {
            String email = users.get(i).getEmail();
            pool.submit(() -> {
                try {
                    start.await();
                    couponService.issue(coupon.getId(), email);
                    success.incrementAndGet();
                } catch (Exception e) {
                    failure.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        Coupon reloaded = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(success.get()).isEqualTo(maxCount);
        assertThat(failure.get()).isEqualTo(requesters - maxCount);
        assertThat(reloaded.getIssuedCount()).isEqualTo(maxCount);

        long issuedForThisCoupon = userCouponRepository.findAll().stream()
                .filter(uc -> uc.getCoupon().getId().equals(coupon.getId()))
                .count();
        assertThat(issuedForThisCoupon).isEqualTo(maxCount);
    }
}
