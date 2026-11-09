package com.kafkademo.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kafkademo.analytics.dto.request.AnalyticsEventRequest;
import com.kafkademo.analytics.dto.response.AnalyticsEventResponse;
import com.kafkademo.analytics.entity.AnalyticsEvent;
import com.kafkademo.analytics.exception.AnalyticsEventNotFoundException;
import com.kafkademo.analytics.exception.DuplicateAnalyticsEventException;
import com.kafkademo.analytics.repository.AnalyticsEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant RECEIVED = Instant.parse("2025-06-01T10:00:00Z");
    private static final UUID ID = UUID.randomUUID();

    @Mock
    private AnalyticsEventRepository repository;

    private AnalyticsEventService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsEventServiceImpl(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createInsertsNewEvent() {
        when(repository.insert(any(AnalyticsEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        AnalyticsEventResponse created = service.create(request(ID));

        assertThat(created.eventId()).isEqualTo(ID);
        assertThat(created.receivedAt()).isEqualTo(NOW);
    }

    @Test
    void createGeneratesEventIdWhenAbsent() {
        when(repository.insert(any(AnalyticsEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.create(request(null)).eventId()).isNotNull();
    }

    @Test
    void createRejectsDuplicateEvent() {
        when(repository.insert(any(AnalyticsEvent.class))).thenThrow(new DuplicateKeyException("dup"));

        assertThatThrownBy(() -> service.create(request(ID))).isInstanceOf(DuplicateAnalyticsEventException.class);
    }

    @Test
    void updateKeepsIdAndReceivedAt() {
        when(repository.findById(ID)).thenReturn(Optional.of(existing()));
        when(repository.save(any(AnalyticsEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        AnalyticsEventResponse updated = service.update(ID, request(UUID.randomUUID()));

        assertThat(updated.eventId()).isEqualTo(ID);
        assertThat(updated.receivedAt()).isEqualTo(RECEIVED);
        assertThat(updated.eventType()).isEqualTo("EMAIL_SENT");
    }

    @Test
    void deleteRemovesExistingEvent() {
        AnalyticsEvent event = existing();
        when(repository.findById(ID)).thenReturn(Optional.of(event));

        service.delete(ID);

        verify(repository).delete(event);
    }

    @Test
    void getUnknownEventThrowsNotFound() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(ID)).isInstanceOf(AnalyticsEventNotFoundException.class);
    }

    private static AnalyticsEventRequest request(UUID eventId) {
        return new AnalyticsEventRequest(eventId, "EMAIL_SENT", "email-events", UUID.randomUUID(), NOW);
    }

    private static AnalyticsEvent existing() {
        return new AnalyticsEvent(ID, "CUSTOMER_CREATED", "customer-events", UUID.randomUUID(), NOW, RECEIVED);
    }
}
