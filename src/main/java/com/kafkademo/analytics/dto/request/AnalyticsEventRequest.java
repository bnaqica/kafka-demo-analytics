package com.kafkademo.analytics.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

/**
 * Body of the create and update analytics event APIs.
 *
 * @param eventId generated on create if absent, and ignored on update
 */
@Builder
public record AnalyticsEventRequest(
        UUID eventId,
        @NotBlank String eventType,
        @NotBlank String sourceTopic,
        UUID customerId,
        @NotNull Instant occurredAt,
        String payload) {}
