package com.kafkademo.analytics.exception;

import java.util.UUID;

public class DuplicateAnalyticsEventException extends RuntimeException {

    public DuplicateAnalyticsEventException(UUID eventId) {
        super("Analytics event " + eventId + " already exists");
    }
}
