package com.kafkademo.analytics.service;

import com.kafkademo.analytics.dto.EventTypeCount;
import com.kafkademo.analytics.dto.response.AnalyticsSummary;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Action service: reads through {@link AnalyticsEventService}, never the repository. */
@RequiredArgsConstructor
@Service
public class AnalyticsSummaryServiceImpl implements AnalyticsSummaryService {

    private final AnalyticsEventService eventService;

    @Override
    public AnalyticsSummary summary() {
        Map<String, Long> byType = new TreeMap<>();
        for (EventTypeCount count : eventService.countByEventType()) {
            byType.put(count.eventType(), count.count());
        }
        long total = byType.values().stream().mapToLong(Long::longValue).sum();
        return new AnalyticsSummary(total, byType);
    }
}
