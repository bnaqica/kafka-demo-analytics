package com.kafkademo.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kafkademo.analytics.dto.request.AnalyticsEventRequest;
import com.kafkademo.analytics.exception.DuplicateAnalyticsEventException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class EventRecordingServiceTest {

    private static final UUID EVENT_ID = UUID.fromString("6dde44f1-7abf-4a6c-b1bb-3902d50ce0f6");
    private static final String EVENT = """
            {"eventId":"6dde44f1-7abf-4a6c-b1bb-3902d50ce0f6","eventType":"customer-change",
             "occurredAt":"2026-11-07T18:11:10.588189Z","customerId":"ca77df95-f94f-4682-93b7-b82f74cb808d",
             "to":"alan@example.com"}
            """;

    @Mock
    private AnalyticsEventService eventService;

    private EventRecordingService service;

    @BeforeEach
    void setUp() {
        service = new EventRecordingService(JsonMapper.builder().build(), eventService);
    }

    @Test
    void recordsEnvelopeOfAnyEvent() {
        assertThat(service.record("customer-change-event", EVENT)).isTrue();

        ArgumentCaptor<AnalyticsEventRequest> captor = ArgumentCaptor.forClass(AnalyticsEventRequest.class);
        verify(eventService).create(captor.capture());
        AnalyticsEventRequest request = captor.getValue();
        assertThat(request.eventId()).isEqualTo(EVENT_ID);
        assertThat(request.eventType()).isEqualTo("customer-change");
        assertThat(request.sourceTopic()).isEqualTo("customer-change-event");
        assertThat(request.occurredAt()).isEqualTo(Instant.parse("2026-11-07T18:11:10.588189Z"));
    }

    @Test
    void savesThePayloadExactlyAsReceived() {
        String addressChange = """
                {"eventId":"6dde44f1-7abf-4a6c-b1bb-3902d50ce0f6","eventType":"customer-address-change",
                 "occurredAt":"2026-11-09T18:04:18Z","customerId":"ca77df95-f94f-4682-93b7-b82f74cb808d",
                 "email":"ada@example.com","changes":{"address":{"city":"Reno"}}}
                """;

        service.record("customer-change-event", addressChange);

        ArgumentCaptor<AnalyticsEventRequest> captor = ArgumentCaptor.forClass(AnalyticsEventRequest.class);
        verify(eventService).create(captor.capture());
        assertThat(captor.getValue().payload()).isEqualTo(addressChange);
        assertThat(captor.getValue().eventType()).isEqualTo("customer-address-change");
    }

    @Test
    void ignoresDuplicateEvent() {
        when(eventService.create(any())).thenThrow(new DuplicateAnalyticsEventException(EVENT_ID));

        assertThat(service.record("customer-change-event", EVENT)).isFalse();
    }

    @Test
    void rejectsEventWithoutEventId() {
        String json = "{\"eventType\":\"customer-change\",\"occurredAt\":\"2026-11-07T18:11:10Z\"}";

        assertThatThrownBy(() -> service.record("customer-change-event", json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eventId");
        verifyNoInteractions(eventService);
    }

    @Test
    void rejectsAnUnreadableOccurredAtSoItIsNotRetried() {
        String json = EVENT.replace("2026-11-07T18:11:10.588189Z", "yesterday");

        assertThatThrownBy(() -> service.record("customer-change-event", json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("occurredAt");
        verifyNoInteractions(eventService);
    }

    @Test
    void rejectsAnEventWithoutACustomerId() {
        String json = EVENT.replace("\"customerId\":\"ca77df95-f94f-4682-93b7-b82f74cb808d\",", "");

        assertThatThrownBy(() -> service.record("customer-change-event", json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customerId");
        verifyNoInteractions(eventService);
    }
}
