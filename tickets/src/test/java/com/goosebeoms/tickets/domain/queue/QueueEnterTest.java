package com.goosebeoms.tickets.domain.queue;

import com.goosebeoms.tickets.domain.queue.dto.QueueStatusResponse;
import com.goosebeoms.tickets.domain.queue.service.QueueService;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class QueueEnterTest extends AbstractIntegrationTest {

    @Autowired QueueService queueService;
    @Autowired StringRedisTemplate redis;
    @Autowired TestDataFactory factory;

    @BeforeEach
    void clean() {
        factory.flushRedis();
    }

    @Test
    void concurrentEntriesGetUniqueRanks() throws InterruptedException {
        ShowSchedule schedule = factory.newSchedule(2);
        int users = 200;
        List<User> entrants = factory.newUsers(users, "queuer");

        Set<Long> seenPositions = ConcurrentHashMap.newKeySet();
        AtomicInteger failures = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(users);
        ExecutorService pool = Executors.newFixedThreadPool(32);

        for (User u : entrants) {
            pool.submit(() -> {
                try {
                    start.await();
                    QueueStatusResponse r = queueService.enter(schedule.getId(), u.getEmail());
                    seenPositions.add(r.position());
                } catch (Exception ignored) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertThat(done.await(120, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        assertThat(failures.get()).as("enter() 호출 중 발생한 예외 수").isZero();

        Long zcard = redis.opsForZSet().zCard(QueueService.waitKey(schedule.getId()));
        assertThat(zcard).as("대기열 ZSET 원소 수").isEqualTo((long) users);
        assertThat(seenPositions).hasSize(users);
    }

    @Test
    void enterIsIdempotentForSameUser() {
        ShowSchedule schedule = factory.newSchedule(1);
        User user = factory.newUser("dup@test.com");

        QueueStatusResponse first = queueService.enter(schedule.getId(), user.getEmail());
        QueueStatusResponse second = queueService.enter(schedule.getId(), user.getEmail());

        assertThat(first.position()).isEqualTo(1L);
        assertThat(second.position()).isEqualTo(1L);
        assertThat(redis.opsForZSet().zCard(QueueService.waitKey(schedule.getId()))).isEqualTo(1L);
    }
}
