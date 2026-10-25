package com.smartsub.dto;

import java.time.LocalDate;

public class TimelineEventDto {
    private Long id;
    private String subscriptionName;
    private String eventType;
    private LocalDate eventDate;
    private String description;
    private String icon;
    private String badgeColor;

    public TimelineEventDto() {}

    public TimelineEventDto(Long id, String subscriptionName, String eventType, LocalDate eventDate, String description, String icon, String badgeColor) {
        this.id = id;
        this.subscriptionName = subscriptionName;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.description = description;
        this.icon = icon;
        this.badgeColor = badgeColor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
