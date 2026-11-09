package com.kafkademo.analytics.entity;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * One event observed on any topic. The producer's {@code eventId} is the document id, so a
 * redelivered event can never be counted twice.
 */
@Builder
@Document("analytics_event")
public record AnalyticsEvent(
        @Id UUID eventId,
        String eventType,
        String sourceTopic,
        UUID customerId,
        Instant occurredAt,
        Instant receivedAt) {}
