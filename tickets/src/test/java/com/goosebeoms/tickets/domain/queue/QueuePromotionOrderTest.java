package com.goosebeoms.tickets.domain.queue;

import com.goosebeoms.tickets.domain.queue.scheduler.QueuePromotionScheduler;
import com.goosebeoms.tickets.domain.queue.service.QueueService;
import com.goosebeoms.tickets.domain.queue.service.QueueTokenService;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "app.queue.active-capacity=3")
class QueuePromotionOrderTest extends AbstractIntegrationTest {

    @Autowired QueueService queueService;
    @Autowired QueuePromotionScheduler scheduler;
    @Autowired StringRedisTemplate redis;
    @Autowired TestDataFactory factory;

    @Value("${app.queue.active-capacity}")
    int capacity;

    @BeforeEach
    void clean() {
        factory.flushRedis();
    }

    @Test
    void promotesEarliestUpToCapacity() throws InterruptedException {
        ShowSchedule schedule = factory.newSchedule(10);
        List<User> users = factory.newUsers(10, "rank");

        for (User u : users) {
            queueService.enter(schedule.getId(), u.getEmail());
            Thread.sleep(2);
        }
        assertThat(capacity).isEqualTo(3);

        scheduler.runOnce();

        Long activeCard = redis.opsForZSet().zCard(QueueService.activeKey(schedule.getId()));
        Long waitCard = redis.opsForZSet().zCard(QueueService.waitKey(schedule.getId()));
        assertThat(activeCard).isEqualTo(3L);
        assertThat(waitCard).isEqualTo(7L);

        Set<String> activeMembers = redis.opsForZSet().range(QueueService.activeKey(schedule.getId()), 0, -1);
        assertThat(activeMembers).containsExactlyInAnyOrder(
                users.get(0).getId().toString(),
                users.get(1).getId().toString(),
                users.get(2).getId().toString()
        );

        for (int i = 0; i < 3; i++) {
            String token = redis.opsForValue().get(
                    QueueTokenService.USER_TOKEN_KEY_PREFIX + schedule.getId() + ":" + users.get(i).getId());
            assertThat(token).isNotNull();
        }
    }
}
