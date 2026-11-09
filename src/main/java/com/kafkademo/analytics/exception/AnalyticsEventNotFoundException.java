package com.kafkademo.analytics.exception;

import java.util.UUID;

public class AnalyticsEventNotFoundException extends RuntimeException {

    public AnalyticsEventNotFoundException(UUID eventId) {
        super("Analytics event " + eventId + " not found");
    }
}
