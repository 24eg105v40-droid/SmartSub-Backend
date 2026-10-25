package com.smartsub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscription_events")
public class SubscriptionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    private String subscriptionName;
    private String eventType; // STARTED, RENEWED, PRICE_CHANGED, PAUSED, RESUMED, CANCELLED, GOAL_COMPLETED
    private LocalDate eventDate;
    private String description;
    private String icon;
    private String badgeColor;

    public SubscriptionEvent() {}

    public SubscriptionEvent(User user, String subscriptionName, String eventType, LocalDate eventDate, String description, String icon, String badgeColor) {
        this.user = user;
        this.subscriptionName = subscriptionName;
        this.eventType = eventType;
        this.eventDate = eventDate != null ? eventDate : LocalDate.now();
        this.description = description;
        this.icon = icon != null ? icon : "fas fa-calendar-check";
        this.badgeColor = badgeColor != null ? badgeColor : "#6366f1";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSubscriptionName() { return subscriptionName; }
    public void setSubscriptionName(String subscriptionName) { this.subscriptionName = subscriptionName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }
}
