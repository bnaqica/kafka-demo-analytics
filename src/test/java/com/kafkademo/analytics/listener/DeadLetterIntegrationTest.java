package com.kafkademo.analytics.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.kafkademo.analytics.TestcontainersConfiguration;
import com.kafkademo.analytics.repository.AnalyticsEventRepository;
import com.kafkademo.analytics.service.EventRecordingService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(
        properties = {
            "spring.kafka.listener.auto-startup=true",
            "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
        })
@EmbeddedKafka(
        partitions = 1,
        topics = {DeadLetterIntegrationTest.TOPIC, DeadLetterIntegrationTest.DLQ})
@Import(TestcontainersConfiguration.class)
class DeadLetterIntegrationTest {

    static final String TOPIC = "customer-change-event";
    static final String DLQ = "customer-change-event-dlq";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AnalyticsEventRepository repository;

    @MockitoSpyBean
    private EventRecordingService recordingService;

    @Value("${spring.embedded.kafka.brokers}")
    private String brokers;

    @Test
    void unusableEventsGoStraightToTheDeadLetterTopicAndAreNotSaved() throws Exception {
        String malformed = "not json " + UUID.randomUUID();
        String badDate = """
                {"eventId":"%s","eventType":"customer-addition","occurredAt":"yesterday","customerId":"%s"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID());
        long before = repository.count();

        kafkaTemplate.send(TOPIC, "a", malformed).get();
        kafkaTemplate.send(TOPIC, "a", badDate).get();

        assertThat(deadLetters(2)).containsExactly(malformed, badDate);
        verify(recordingService, timeout(1000).times(1)).record(TOPIC, malformed);
        verify(recordingService, times(1)).record(TOPIC, badDate);
        assertThat(repository.count()).isEqualTo(before);
    }

    private List<String> deadLetters(int expected) {
        Map<String, Object> config = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                brokers,
                ConsumerConfig.GROUP_ID_CONFIG,
                UUID.randomUUID().toString(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        List<String> values = new java.util.ArrayList<>();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config)) {
            consumer.subscribe(List.of(DLQ));
            long deadline = System.currentTimeMillis() + 15000;
            while (values.size() < expected && System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(200));
                records.forEach(record -> values.add(record.value()));
            }
        }
        return values;
    }
}
