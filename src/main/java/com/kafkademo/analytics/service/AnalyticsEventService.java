package com.kafkademo.analytics.service;

import com.kafkademo.analytics.dto.EventTypeCount;
import com.kafkademo.analytics.dto.request.AnalyticsEventRequest;
import com.kafkademo.analytics.dto.response.AnalyticsEventResponse;
import java.util.List;
import java.util.UUID;

public interface AnalyticsEventService {

    AnalyticsEventResponse create(AnalyticsEventRequest request);

    List<AnalyticsEventResponse> findRecent(int limit);

    AnalyticsEventResponse get(UUID eventId);

    AnalyticsEventResponse update(UUID eventId, AnalyticsEventRequest request);

    void delete(UUID eventId);

    List<EventTypeCount> countByEventType();
}
