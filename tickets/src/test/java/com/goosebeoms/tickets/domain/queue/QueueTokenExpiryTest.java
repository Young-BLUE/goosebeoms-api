package com.goosebeoms.tickets.domain.queue;

import com.goosebeoms.tickets.domain.queue.scheduler.QueuePromotionScheduler;
import com.goosebeoms.tickets.domain.queue.service.QueueService;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "app.queue.active-capacity=1")
class QueueTokenExpiryTest extends AbstractIntegrationTest {

    @Autowired QueueService queueService;
    @Autowired QueuePromotionScheduler scheduler;
    @Autowired StringRedisTemplate redis;
    @Autowired TestDataFactory factory;

    @BeforeEach
    void clean() {
        factory.flushRedis();
    }

    @Test
    void expiredActiveSlotIsReassignedToNextWaiting() throws InterruptedException {
        ShowSchedule schedule = factory.newSchedule(2);
        User first = factory.newUser("first@test.com");
        User second = factory.newUser("second@test.com");

        queueService.enter(schedule.getId(), first.getEmail());
        Thread.sleep(2);
        queueService.enter(schedule.getId(), second.getEmail());

        scheduler.runOnce();

        Long activeCard = redis.opsForZSet().zCard(QueueService.activeKey(schedule.getId()));
        assertThat(activeCard).isEqualTo(1L);
        Set<String> activeMembers = redis.opsForZSet().range(QueueService.activeKey(schedule.getId()), 0, -1);
        assertThat(activeMembers).containsExactly(first.getId().toString());

        redis.opsForZSet().add(
                QueueService.activeKey(schedule.getId()),
                first.getId().toString(),
                System.currentTimeMillis() - 1000
        );

        scheduler.runOnce();

        activeMembers = redis.opsForZSet().range(QueueService.activeKey(schedule.getId()), 0, -1);
        assertThat(activeMembers).containsExactly(second.getId().toString());

        Long waitCard = redis.opsForZSet().zCard(QueueService.waitKey(schedule.getId()));
        assertThat(waitCard).isEqualTo(0L);
    }
}
