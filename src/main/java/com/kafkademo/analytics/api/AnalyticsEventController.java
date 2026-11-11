package com.kafkademo.analytics.api;

import com.kafkademo.analytics.config.ApiRoles;
import com.kafkademo.analytics.dto.request.AnalyticsEventRequest;
import com.kafkademo.analytics.dto.response.AnalyticsEventResponse;
import com.kafkademo.analytics.service.AnalyticsEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/analytics/events")
public class AnalyticsEventController {

    private final AnalyticsEventService eventService;

    @PreAuthorize(ApiRoles.ADMIN)
    @PostMapping
    public ResponseEntity<AnalyticsEventResponse> create(@Valid @RequestBody AnalyticsEventRequest request) {
        AnalyticsEventResponse created = eventService.create(request);
        return ResponseEntity.created(URI.create("/api/analytics/events/" + created.eventId()))
                .body(created);
    }

    @PreAuthorize(ApiRoles.READ)
    @GetMapping
    public List<AnalyticsEventResponse> recent(@RequestParam(defaultValue = "20") @Min(1) @Max(200) int limit) {
        return eventService.findRecent(limit);
    }

    @PreAuthorize(ApiRoles.READ)
    @GetMapping("/{eventId}")
    public AnalyticsEventResponse get(@PathVariable UUID eventId) {
        return eventService.get(eventId);
    }

    @PreAuthorize(ApiRoles.ADMIN)
    @PutMapping("/{eventId}")
    public AnalyticsEventResponse update(
            @PathVariable UUID eventId, @Valid @RequestBody AnalyticsEventRequest request) {
        return eventService.update(eventId, request);
    }

    @PreAuthorize(ApiRoles.ADMIN)
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID eventId) {
        eventService.delete(eventId);
    }
}
