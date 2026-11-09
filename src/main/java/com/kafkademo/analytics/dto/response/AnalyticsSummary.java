package com.kafkademo.analytics.dto.response;

import java.util.Map;

public record AnalyticsSummary(long totalEvents, Map<String, Long> eventsByType) {}
