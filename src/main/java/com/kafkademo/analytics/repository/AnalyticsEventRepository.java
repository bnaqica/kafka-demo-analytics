package com.kafkademo.analytics.repository;

import com.kafkademo.analytics.dto.EventTypeCount;
import com.kafkademo.analytics.entity.AnalyticsEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnalyticsEventRepository extends MongoRepository<AnalyticsEvent, UUID> {

    List<AnalyticsEvent> findAllByOrderByOccurredAtDesc(Pageable pageable);

    @Aggregation("{ $group: { _id: '$eventType', count: { $sum: 1 } } }")
    List<EventTypeCount> countByEventType();
}
