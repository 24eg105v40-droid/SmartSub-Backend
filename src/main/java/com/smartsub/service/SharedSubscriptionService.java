package com.smartsub.service;

import com.smartsub.dto.SharedMemberRequest;
import com.smartsub.model.SharedMember;
import com.smartsub.model.Subscription;
import com.smartsub.model.User;
import com.smartsub.repository.SharedMemberRepository;
import com.smartsub.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SharedSubscriptionService {

    @Autowired
    private SharedMemberRepository sharedMemberRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserService userService;

    public List<SharedMember> getMembersForSubscription(Long subscriptionId) {
        User user = userService.getAuthenticatedUser();
        Subscription sub = subscriptionRepository.findByIdAndUser(subscriptionId, user)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with ID: " + subscriptionId));
        return sharedMemberRepository.findBySubscription(sub);
    }

    @Transactional
    public SharedMember addMember(SharedMemberRequest request) {
        User user = userService.getAuthenticatedUser();
        Subscription sub = subscriptionRepository.findByIdAndUser(request.getSubscriptionId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with ID: " + request.getSubscriptionId()));

        SharedMember member = new SharedMember(
                sub,
                request.getMemberName(),
                request.getMemberEmail(),
                request.getShareAmount(),
                request.getIsPaid(),
                request.getNotes()
        );

        return sharedMemberRepository.save(member);
    }

    @Transactional
    public SharedMember togglePaymentStatus(Long memberId, Boolean isPaid) {
        User user = userService.getAuthenticatedUser();
        SharedMember member = sharedMemberRepository.findByIdAndSubscription_User(memberId, user)
                .orElseThrow(() -> new IllegalArgumentException("Shared member not found or access denied for ID: " + memberId));

        member.setIsPaid(isPaid);
        return sharedMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long memberId) {
        User user = userService.getAuthenticatedUser();
        SharedMember member = sharedMemberRepository.findByIdAndSubscription_User(memberId, user)
                .orElseThrow(() -> new IllegalArgumentException("Shared member not found or access denied for ID: " + memberId));
        sharedMemberRepository.delete(member);
    }
}
