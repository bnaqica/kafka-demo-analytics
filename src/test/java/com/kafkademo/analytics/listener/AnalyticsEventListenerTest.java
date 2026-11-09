package com.kafkademo.analytics.listener;

import static org.mockito.Mockito.verify;

import com.kafkademo.analytics.service.EventRecordingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventListenerTest {

    @Mock
    private EventRecordingService eventRecordingService;

    @InjectMocks
    private AnalyticsEventListener listener;

    @Test
    void passesTopicAndPayloadToService() {
        listener.onEvent(new ConsumerRecord<>("email-events", 0, 0L, "key", "{\"a\":1}"));

        verify(eventRecordingService).record("email-events", "{\"a\":1}");
    }
}
