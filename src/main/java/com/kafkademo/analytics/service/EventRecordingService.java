package com.kafkademo.analytics.service;

import com.kafkademo.analytics.dto.request.AnalyticsEventRequest;
import com.kafkademo.analytics.exception.DuplicateAnalyticsEventException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Action service: records events consumed from Kafka, changing data only through {@link AnalyticsEventService}.
 * Only the envelope fields every event shares (eventId, eventType, occurredAt, customerId) are read, and the rest is
 * saved as received, so new event types are recorded without code changes. See docs/kafka-events.md.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EventRecordingService {

    private final JsonMapper jsonMapper;
    private final AnalyticsEventService eventService;

    /** Returns false if this eventId was already recorded (at-least-once redelivery). */
    public boolean record(String topic, String payload) {
        JsonNode node = jsonMapper.readTree(payload);
        UUID eventId = UUID.fromString(requireText(node, "eventId"));
        AnalyticsEventRequest request = AnalyticsEventRequest.builder()
                .eventId(eventId)
                .eventType(requireText(node, "eventType"))
                .sourceTopic(topic)
                .customerId(UUID.fromString(requireText(node, "customerId")))
                .occurredAt(occurredAt(node))
                .payload(payload)
                .build();
        try {
            eventService.create(request);
            return true;
        } catch (DuplicateAnalyticsEventException ex) {
            log.info("Event {} already recorded, skipping", eventId);
            return false;
        }
    }

    private static Instant occurredAt(JsonNode node) {
        try {
            return Instant.parse(requireText(node, "occurredAt"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Event has an unreadable 'occurredAt'", ex);
        }
    }

    private static String requireText(JsonNode node, String field) {
        String value = node.path(field).asString(null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Event is missing required field '" + field + "'");
        }
        return value;
    }
}
