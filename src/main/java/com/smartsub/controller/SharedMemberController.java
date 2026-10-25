package com.smartsub.controller;

import com.smartsub.dto.SharedMemberRequest;
import com.smartsub.model.SharedMember;
import com.smartsub.service.SharedSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shared")
public class SharedMemberController {

    @Autowired
    private SharedSubscriptionService sharedSubscriptionService;

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<SharedMember>> getMembers(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(sharedSubscriptionService.getMembersForSubscription(subscriptionId));
    }

    @PostMapping("/members")
    public ResponseEntity<SharedMember> addMember(@Valid @RequestBody SharedMemberRequest request) {
        return ResponseEntity.ok(sharedSubscriptionService.addMember(request));
    }

    @PatchMapping("/members/{memberId}/paid")
    public ResponseEntity<SharedMember> togglePaid(@PathVariable Long memberId, @RequestBody Map<String, Boolean> payload) {
        Boolean isPaid = payload.getOrDefault("isPaid", true);
        return ResponseEntity.ok(sharedSubscriptionService.togglePaymentStatus(memberId, isPaid));
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Map<String, String>> removeMember(@PathVariable Long memberId) {
        sharedSubscriptionService.removeMember(memberId);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }
}
