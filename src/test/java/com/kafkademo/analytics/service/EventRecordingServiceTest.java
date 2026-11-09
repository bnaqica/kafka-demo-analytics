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
    private static final String EMAIL_SENT = """
            {"eventId":"6dde44f1-7abf-4a6c-b1bb-3902d50ce0f6","eventType":"EMAIL_SENT",
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
        assertThat(service.record("email-events", EMAIL_SENT)).isTrue();

        ArgumentCaptor<AnalyticsEventRequest> captor = ArgumentCaptor.forClass(AnalyticsEventRequest.class);
        verify(eventService).create(captor.capture());
        AnalyticsEventRequest request = captor.getValue();
        assertThat(request.eventId()).isEqualTo(EVENT_ID);
        assertThat(request.eventType()).isEqualTo("EMAIL_SENT");
        assertThat(request.sourceTopic()).isEqualTo("email-events");
        assertThat(request.occurredAt()).isEqualTo(Instant.parse("2026-11-07T18:11:10.588189Z"));
    }

    @Test
    void ignoresDuplicateEvent() {
        when(eventService.create(any())).thenThrow(new DuplicateAnalyticsEventException(EVENT_ID));

        assertThat(service.record("email-events", EMAIL_SENT)).isFalse();
    }

    @Test
    void rejectsEventWithoutEventId() {
        String json = "{\"eventType\":\"EMAIL_SENT\",\"occurredAt\":\"2026-11-07T18:11:10Z\"}";

        assertThatThrownBy(() -> service.record("email-events", json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eventId");
        verifyNoInteractions(eventService);
    }
}
