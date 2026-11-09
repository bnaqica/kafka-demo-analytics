package com.kafkademo.analytics.listener;

import com.kafkademo.analytics.service.EventRecordingService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Listens to every topic in {@code app.kafka.topics}. */
@RequiredArgsConstructor
@Component
public class AnalyticsEventListener {

    private final EventRecordingService eventRecordingService;

    @KafkaListener(topics = "#{'${app.kafka.topics}'.split(',')}")
    public void onEvent(ConsumerRecord<String, String> record) {
        eventRecordingService.record(record.topic(), record.value());
    }
}
