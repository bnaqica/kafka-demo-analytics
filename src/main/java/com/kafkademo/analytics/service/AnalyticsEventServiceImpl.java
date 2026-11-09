package com.kafkademo.analytics.service;

import com.kafkademo.analytics.dto.EventTypeCount;
import com.kafkademo.analytics.dto.request.AnalyticsEventRequest;
import com.kafkademo.analytics.dto.response.AnalyticsEventResponse;
import com.kafkademo.analytics.entity.AnalyticsEvent;
import com.kafkademo.analytics.exception.AnalyticsEventNotFoundException;
import com.kafkademo.analytics.exception.DuplicateAnalyticsEventException;
import com.kafkademo.analytics.repository.AnalyticsEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnalyticsEventServiceImpl implements AnalyticsEventService {

    private final AnalyticsEventRepository repository;
    private final Clock clock;

    @Override
    public AnalyticsEventResponse create(AnalyticsEventRequest request) {
        UUID eventId = request.eventId() != null ? request.eventId() : UUID.randomUUID();
        try {
            AnalyticsEvent saved = repository.insert(toEntity(eventId, request, Instant.now(clock)));
            log.info("Recorded {} {} from {}", saved.eventType(), saved.eventId(), saved.sourceTopic());
            return toResponse(saved);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateAnalyticsEventException(eventId);
        }
    }

    @Override
    public List<AnalyticsEventResponse> findRecent(int limit) {
        return repository.findAllByOrderByOccurredAtDesc(PageRequest.of(0, limit)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AnalyticsEventResponse get(UUID eventId) {
        return toResponse(find(eventId));
    }

    @Override
    public AnalyticsEventResponse update(UUID eventId, AnalyticsEventRequest request) {
        AnalyticsEvent saved =
                repository.save(toEntity(eventId, request, find(eventId).receivedAt()));
        log.info("Updated analytics event {}", eventId);
        return toResponse(saved);
    }

    @Override
    public void delete(UUID eventId) {
        repository.delete(find(eventId));
        log.info("Deleted analytics event {}", eventId);
    }

    @Override
    public List<EventTypeCount> countByEventType() {
        return repository.countByEventType();
    }

    private AnalyticsEvent find(UUID eventId) {
        return repository.findById(eventId).orElseThrow(() -> new AnalyticsEventNotFoundException(eventId));
    }

    private static AnalyticsEvent toEntity(UUID eventId, AnalyticsEventRequest request, Instant receivedAt) {
        return AnalyticsEvent.builder()
                .eventId(eventId)
                .eventType(request.eventType())
                .sourceTopic(request.sourceTopic())
                .customerId(request.customerId())
                .occurredAt(request.occurredAt())
                .receivedAt(receivedAt)
                .build();
    }

    private AnalyticsEventResponse toResponse(AnalyticsEvent event) {
        return AnalyticsEventResponse.builder()
                .eventId(event.eventId())
                .eventType(event.eventType())
                .sourceTopic(event.sourceTopic())
                .customerId(event.customerId())
                .occurredAt(event.occurredAt())
                .receivedAt(event.receivedAt())
                .build();
    }
}
