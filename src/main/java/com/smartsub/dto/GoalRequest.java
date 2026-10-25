package com.smartsub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class GoalRequest {
    @NotNull(message = "Subscription ID is required")
    private Long subscriptionId;

    @NotBlank(message = "Goal title is required")
    private String goalTitle;

    private String goalDescription;
    private Integer progressPercentage = 0;
    private LocalDate targetDate;

    public GoalRequest() {}

    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getGoalTitle() { return goalTitle; }
    public void setGoalTitle(String goalTitle) { this.goalTitle = goalTitle; }

    public String getGoalDescription() { return goalDescription; }
    public void setGoalDescription(String goalDescription) { this.goalDescription = goalDescription; }

    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
}
