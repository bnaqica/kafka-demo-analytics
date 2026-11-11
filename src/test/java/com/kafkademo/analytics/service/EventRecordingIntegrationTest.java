package com.kafkademo.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kafkademo.analytics.TestcontainersConfiguration;
import com.kafkademo.analytics.repository.AnalyticsEventRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class EventRecordingIntegrationTest {

    private static final String TOPIC = "customer-change-event";

    @Autowired
    private EventRecordingService service;

    @Autowired
    private AnalyticsSummaryService summaryService;

    @Autowired
    private AnalyticsEventRepository repository;

    @Value("${app.kafka.topics}")
    private String subscribedTopics;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void subscribesOnlyToTheCustomerChangeTopic() {
        assertThat(subscribedTopics).isEqualTo(TOPIC);
    }

    @Test
    void redeliveredEventLeavesTheFirstRecordUnchanged() {
        UUID eventId = UUID.randomUUID();
        String first = event(eventId, "customer-addition", "first");
        String redelivered = event(eventId, "customer-addition", "second");

        assertThat(service.record(TOPIC, first)).isTrue();
        assertThat(service.record(TOPIC, redelivered)).isFalse();

        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findById(eventId).orElseThrow().payload()).isEqualTo(first);
    }

    @Test
    void summaryCountsEachType() {
        service.record(TOPIC, event(UUID.randomUUID(), "customer-addition", "a"));
        service.record(TOPIC, event(UUID.randomUUID(), "customer-change", "b"));
        service.record(TOPIC, event(UUID.randomUUID(), "customer-change", "c"));

        assertThat(summaryService.summary().totalEvents()).isEqualTo(3);
        assertThat(summaryService.summary().eventsByType())
                .isEqualTo(Map.of("customer-addition", 1L, "customer-change", 2L));
    }

    private static String event(UUID eventId, String type, String firstName) {
        return """
                {"eventId":"%s","eventType":"%s","occurredAt":"2026-11-09T18:04:18Z",
                 "customerId":"%s","firstName":"%s"}
                """.formatted(eventId, type, UUID.randomUUID(), firstName);
    }
}
