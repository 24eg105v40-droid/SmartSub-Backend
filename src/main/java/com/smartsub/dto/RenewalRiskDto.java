package com.smartsub.dto;

import java.time.LocalDate;

public class RenewalRiskDto {
    private Long subscriptionId;
    private String serviceName;
    private String category;
    private Double cost;
    private String billingCycle;
    private LocalDate renewalDate;
    private long daysRemaining;
    private int usageDays;
    private int healthScore;
    private String riskLevel; // HIGH_ATTENTION (Red), MEDIUM_RISK (Yellow), LOW_RISK (Green)
    private String riskBadgeColor;
    private String reason;
    private String actionNeeded;

    public RenewalRiskDto() {}

    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public LocalDate getRenewalDate() { return renewalDate; }
    public void setRenewalDate(LocalDate renewalDate) { this.renewalDate = renewalDate; }

    public long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }

    public int getUsageDays() { return usageDays; }
    public void setUsageDays(int usageDays) { this.usageDays = usageDays; }

    public int getHealthScore() { return healthScore; }
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskBadgeColor() { return riskBadgeColor; }
    public void setRiskBadgeColor(String riskBadgeColor) { this.riskBadgeColor = riskBadgeColor; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getActionNeeded() { return actionNeeded; }
    public void setActionNeeded(String actionNeeded) { this.actionNeeded = actionNeeded; }
}
