package com.kafkademo.analytics.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AnalyticsEventResponse(
        UUID eventId,
        String eventType,
        String sourceTopic,
        UUID customerId,
        Instant occurredAt,
        Instant receivedAt,
        String payload) {}
