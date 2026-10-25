package com.smartsub.controller;

import com.smartsub.dto.GoalRequest;
import com.smartsub.model.SubscriptionGoal;
import com.smartsub.service.GoalTimelineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    private GoalTimelineService goalTimelineService;

    @GetMapping
    public ResponseEntity<List<SubscriptionGoal>> getGoals() {
        return ResponseEntity.ok(goalTimelineService.getUserGoals());
    }

    @PostMapping
    public ResponseEntity<SubscriptionGoal> createGoal(@Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(goalTimelineService.createGoal(request));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<SubscriptionGoal> updateProgress(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Integer progress = payload.getOrDefault("progressPercentage", 0);
        return ResponseEntity.ok(goalTimelineService.updateGoalProgress(id, progress));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id) {
        goalTimelineService.deleteGoal(id);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
