package com.kafkademo.analytics.dto;

import org.springframework.data.annotation.Id;

public record EventTypeCount(@Id String eventType, long count) {}
