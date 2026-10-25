package com.smartsub.controller;

import com.smartsub.dto.PriceChangeRequest;
import com.smartsub.dto.SubscriptionRequest;
import com.smartsub.dto.SubscriptionResponse;
import com.smartsub.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions(@RequestParam(required = false) Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) {
            return ResponseEntity.ok(subscriptionService.getActiveSubscriptions());
        }
        return ResponseEntity.ok(subscriptionService.getAllUserSubscriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.createSubscription(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> updateSubscription(@PathVariable Long id, @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, request));
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<SubscriptionResponse> recordPriceChange(@PathVariable Long id, @Valid @RequestBody PriceChangeRequest request) {
        return ResponseEntity.ok(subscriptionService.recordPriceChange(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SubscriptionResponse> toggleStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.getOrDefault("status", "ACTIVE");
        return ResponseEntity.ok(subscriptionService.toggleStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.ok(Map.of("message", "Subscription deleted successfully"));
    }
}
