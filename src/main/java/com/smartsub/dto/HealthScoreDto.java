package com.smartsub.dto;

public class HealthScoreDto {
    private Long subscriptionId;
    private String serviceName;
    private String category;
    private Double monthlyCost;
    private Integer usageDays;
    private Integer valueRating;
    private Integer score; // 0 - 100
    private String status; // EXCELLENT, GOOD, ATTENTION_NEEDED, POOR
    private String badgeColor; // #10b981 (green), #f59e0b (amber), #ef4444 (red)
    private String feedback;

    public HealthScoreDto() {}

    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getMonthlyCost() { return monthlyCost; }
    public void setMonthlyCost(Double monthlyCost) { this.monthlyCost = monthlyCost; }

    public Integer getUsageDays() { return usageDays; }
    public void setUsageDays(Integer usageDays) { this.usageDays = usageDays; }

    public Integer getValueRating() { return valueRating; }
    public void setValueRating(Integer valueRating) { this.valueRating = valueRating; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
