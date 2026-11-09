package com.kafkademo.analytics.api;

import com.kafkademo.analytics.dto.response.AnalyticsSummary;
import com.kafkademo.analytics.service.AnalyticsSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsSummaryService summaryService;

    @GetMapping("/summary")
    public AnalyticsSummary summary() {
        return summaryService.summary();
    }
}
