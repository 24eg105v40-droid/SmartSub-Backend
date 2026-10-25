package com.smartsub.controller;

import com.smartsub.dto.*;
import com.smartsub.service.IntelligenceAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private IntelligenceAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/health-scores")
    public ResponseEntity<List<HealthScoreDto>> getHealthScores() {
        return ResponseEntity.ok(analyticsService.getHealthScores());
    }

    @GetMapping("/cost-per-use")
    public ResponseEntity<List<CostPerUseDto>> getCostPerUseAnalysis() {
        return ResponseEntity.ok(analyticsService.getCostPerUseAnalysis());
    }

    @GetMapping("/overlaps")
    public ResponseEntity<List<OverlapGroupDto>> getOverlaps() {
        return ResponseEntity.ok(analyticsService.detectOverlaps());
    }

    @GetMapping("/future-forecast")
    public ResponseEntity<FutureForecastDto> getFutureForecast() {
        return ResponseEntity.ok(analyticsService.getFutureForecast());
    }

    @GetMapping("/renewal-risks")
    public ResponseEntity<List<RenewalRiskDto>> getRenewalRisks() {
        return ResponseEntity.ok(analyticsService.getRenewalRisks());
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<RenewalCalendarItemDto>> getRenewalCalendar(@RequestParam(required = false, defaultValue = "all") String filter) {
        return ResponseEntity.ok(analyticsService.getRenewalCalendar(filter));
    }

    @PostMapping("/simulate-savings")
    public ResponseEntity<SavingsSimulationResultDto> simulateSavings(@RequestBody(required = false) SavingsSimulationRequest request) {
        return ResponseEntity.ok(analyticsService.simulateSavings(request));
    }
}
