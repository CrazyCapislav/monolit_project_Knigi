package dev.petr.book.infrastructure.config;

import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Scheduler;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SchedulerConfigTest {

    @Test
    void jdbcSchedulerBean_created() {
        SchedulerConfig config = new SchedulerConfig();
        Scheduler scheduler = config.jdbcScheduler();
        assertNotNull(scheduler);
    }
}
