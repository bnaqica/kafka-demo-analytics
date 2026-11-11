package com.kafkademo.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.kafkademo.analytics.dto.EventTypeCount;
import com.kafkademo.analytics.dto.response.AnalyticsSummary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsSummaryServiceImplTest {

    @Mock
    private AnalyticsEventService eventService;

    @InjectMocks
    private AnalyticsSummaryServiceImpl service;

    @Test
    void summaryTotalsCountsByType() {
        when(eventService.countByEventType())
                .thenReturn(
                        List.of(new EventTypeCount("customer-change", 2), new EventTypeCount("customer-addition", 3)));

        AnalyticsSummary summary = service.summary();

        assertThat(summary.totalEvents()).isEqualTo(5);
        assertThat(summary.eventsByType())
                .containsEntry("customer-addition", 3L)
                .containsEntry("customer-change", 2L);
    }
}
