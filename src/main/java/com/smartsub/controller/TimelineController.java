package com.smartsub.controller;

import com.smartsub.dto.TimelineEventDto;
import com.smartsub.service.GoalTimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

    @Autowired
    private GoalTimelineService goalTimelineService;

    @GetMapping
    public ResponseEntity<List<TimelineEventDto>> getTimeline() {
        return ResponseEntity.ok(goalTimelineService.getUserTimeline());
    }
}
