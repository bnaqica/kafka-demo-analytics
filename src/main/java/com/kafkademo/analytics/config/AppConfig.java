package com.kafkademo.analytics.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

@Slf4j
@Configuration
public class AppConfig {
    /**
     * Retries a failing record 3 times, one second apart, then publishes it to {@code <topic>-dlq}.
     * Malformed JSON is never going to succeed, so it goes straight to the DLQ. Spring Boot wires
     * this bean into the default listener container factory.
     */
    @Bean
    CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, (record, ex) -> new TopicPartition(record.topic() + "-dlq", -1));
        DefaultErrorHandler handler = new DefaultErrorHandler(
                (record, ex) -> {
                    log.error("Sending {}-{}@{} to DLQ", record.topic(), record.partition(), record.offset(), ex);
                    recoverer.accept(record, ex);
                },
                new FixedBackOff(1000L, 3L));
        handler.addNotRetryableExceptions(JacksonException.class, IllegalArgumentException.class);
        return handler;
    }
}
