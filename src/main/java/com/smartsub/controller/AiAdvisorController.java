package com.smartsub.controller;

import com.smartsub.dto.AiAdvisorRequestDto;
import com.smartsub.dto.AiAdvisorResponseDto;
import com.smartsub.service.AiAdvisorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAdvisorController {

    @Autowired
    private AiAdvisorService aiAdvisorService;

    @PostMapping("/advisor")
    public ResponseEntity<AiAdvisorResponseDto> getAdvice(@Valid @RequestBody AiAdvisorRequestDto request) {
        return ResponseEntity.ok(aiAdvisorService.getAdvice(request));
    }
}
