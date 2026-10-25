package com.smartsub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscription_goals")
public class SubscriptionGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    @JsonIgnore
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    private String goalTitle; // e.g. "Complete Java Certification"
    private String goalDescription;
    private Integer progressPercentage = 0; // 0 to 100
    private LocalDate targetDate;
    private Boolean isCompleted = false;

    public SubscriptionGoal() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Subscription getSubscription() { return subscription; }
    public void setSubscription(Subscription subscription) { this.subscription = subscription; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getGoalTitle() { return goalTitle; }
    public void setGoalTitle(String goalTitle) { this.goalTitle = goalTitle; }

    public String getGoalDescription() { return goalDescription; }
    public void setGoalDescription(String goalDescription) { this.goalDescription = goalDescription; }

    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
        if (progressPercentage != null && progressPercentage >= 100) {
            this.isCompleted = true;
        }
    }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
}
