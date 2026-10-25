package com.smartsub.service;

import com.smartsub.dto.GoalRequest;
import com.smartsub.dto.TimelineEventDto;
import com.smartsub.model.Subscription;
import com.smartsub.model.SubscriptionEvent;
import com.smartsub.model.SubscriptionGoal;
import com.smartsub.model.User;
import com.smartsub.repository.SubscriptionEventRepository;
import com.smartsub.repository.SubscriptionGoalRepository;
import com.smartsub.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalTimelineService {

    @Autowired
    private SubscriptionGoalRepository goalRepository;

    @Autowired
    private SubscriptionEventRepository eventRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserService userService;

    public List<SubscriptionGoal> getUserGoals() {
        User user = userService.getAuthenticatedUser();
        return goalRepository.findByUser(user);
    }

    @Transactional
    public SubscriptionGoal createGoal(GoalRequest request) {
        User user = userService.getAuthenticatedUser();
        Subscription sub = subscriptionRepository.findByIdAndUser(request.getSubscriptionId(), user)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with ID: " + request.getSubscriptionId()));

        SubscriptionGoal goal = new SubscriptionGoal();
        goal.setUser(user);
        goal.setSubscription(sub);
        goal.setGoalTitle(request.getGoalTitle());
        goal.setGoalDescription(request.getGoalDescription());
        goal.setProgressPercentage(request.getProgressPercentage() != null ? request.getProgressPercentage() : 0);
        goal.setTargetDate(request.getTargetDate());

        SubscriptionGoal saved = goalRepository.save(goal);

        SubscriptionEvent event = new SubscriptionEvent(
                user,
                sub.getServiceName(),
                "GOAL_ATTACHED",
                LocalDate.now(),
                "Set goal: '" + goal.getGoalTitle() + "' for " + sub.getServiceName(),
                "fas fa-bullseye",
                "#8b5cf6"
        );
        eventRepository.save(event);

        return saved;
    }

    @Transactional
    public SubscriptionGoal updateGoalProgress(Long goalId, Integer progressPercentage) {
        User user = userService.getAuthenticatedUser();
        SubscriptionGoal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        int oldProgress = goal.getProgressPercentage() != null ? goal.getProgressPercentage() : 0;
        goal.setProgressPercentage(progressPercentage);

        if (progressPercentage >= 100 && oldProgress < 100) {
            goal.setIsCompleted(true);
            SubscriptionEvent event = new SubscriptionEvent(
                    user,
                    goal.getSubscription() != null ? goal.getSubscription().getServiceName() : "Subscription Goal",
                    "GOAL_COMPLETED",
                    LocalDate.now(),
                    "🎯 Goal Completed! '" + goal.getGoalTitle() + "' (100%). Review subscription continuation.",
                    "fas fa-trophy",
                    "#10b981"
            );
            eventRepository.save(event);
        }

        return goalRepository.save(goal);
    }

    @Transactional
    public void deleteGoal(Long goalId) {
        User user = userService.getAuthenticatedUser();
        SubscriptionGoal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));
        goalRepository.delete(goal);
    }

    public List<TimelineEventDto> getUserTimeline() {
        User user = userService.getAuthenticatedUser();
        return eventRepository.findByUserOrderByEventDateDesc(user).stream()
                .map(e -> new TimelineEventDto(
                        e.getId(),
                        e.getSubscriptionName(),
                        e.getEventType(),
                        e.getEventDate(),
                        e.getDescription(),
                        e.getIcon(),
                        e.getBadgeColor()
                ))
                .collect(Collectors.toList());
    }
}
