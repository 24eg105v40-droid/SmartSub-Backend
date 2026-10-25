package com.smartsub.dto;

import com.smartsub.model.PriceHistory;
import com.smartsub.model.SharedMember;
import com.smartsub.model.SubscriptionGoal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionResponse {

    private Long id;
    private String serviceName;
    private String planName;
    private Double cost;
    private String billingCycle;
    private LocalDate renewalDate;
    private String category;
    private Integer usageDaysPerMonth;
    private Double usageHoursPerWeek;
    private Integer valueRating;
    private String personalReason;
    private String status;
    private String icon;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived Financial & Health Metrics
    private Double monthlyCostEquivalent;
    private Double yearlyCostEquivalent;
    private Long daysUntilRenewal;
    private Integer healthScore;
    private String healthScoreStatus; // EXCELLENT, GOOD, ATTENTION_NEEDED, POOR
    private Double costPerUse;
    private String usageCategory; // HIGH_USAGE, OPTIMAL, LOW_USAGE
    private String renewalRiskLevel; // LOW_RISK, MEDIUM_RISK, HIGH_ATTENTION
    private String renewalRiskReason;

    private List<PriceHistory> priceHistories;
    private List<SharedMember> sharedMembers;
    private List<SubscriptionGoal> goals;

    public SubscriptionResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public LocalDate getRenewalDate() { return renewalDate; }
    public void setRenewalDate(LocalDate renewalDate) { this.renewalDate = renewalDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getUsageDaysPerMonth() { return usageDaysPerMonth; }
    public void setUsageDaysPerMonth(Integer usageDaysPerMonth) { this.usageDaysPerMonth = usageDaysPerMonth; }

    public Double getUsageHoursPerWeek() { return usageHoursPerWeek; }
    public void setUsageHoursPerWeek(Double usageHoursPerWeek) { this.usageHoursPerWeek = usageHoursPerWeek; }

    public Integer getValueRating() { return valueRating; }
    public void setValueRating(Integer valueRating) { this.valueRating = valueRating; }

    public String getPersonalReason() { return personalReason; }
    public void setPersonalReason(String personalReason) { this.personalReason = personalReason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Double getMonthlyCostEquivalent() { return monthlyCostEquivalent; }
    public void setMonthlyCostEquivalent(Double monthlyCostEquivalent) { this.monthlyCostEquivalent = monthlyCostEquivalent; }

    public Double getYearlyCostEquivalent() { return yearlyCostEquivalent; }
    public void setYearlyCostEquivalent(Double yearlyCostEquivalent) { this.yearlyCostEquivalent = yearlyCostEquivalent; }

    public Long getDaysUntilRenewal() { return daysUntilRenewal; }
    public void setDaysUntilRenewal(Long daysUntilRenewal) { this.daysUntilRenewal = daysUntilRenewal; }

    public Integer getHealthScore() { return healthScore; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }

    public String getHealthScoreStatus() { return healthScoreStatus; }
    public void setHealthScoreStatus(String healthScoreStatus) { this.healthScoreStatus = healthScoreStatus; }

    public Double getCostPerUse() { return costPerUse; }
    public void setCostPerUse(Double costPerUse) { this.costPerUse = costPerUse; }

    public String getUsageCategory() { return usageCategory; }
    public void setUsageCategory(String usageCategory) { this.usageCategory = usageCategory; }

    public String getRenewalRiskLevel() { return renewalRiskLevel; }
    public void setRenewalRiskLevel(String renewalRiskLevel) { this.renewalRiskLevel = renewalRiskLevel; }

    public String getRenewalRiskReason() { return renewalRiskReason; }
    public void setRenewalRiskReason(String renewalRiskReason) { this.renewalRiskReason = renewalRiskReason; }

    public List<PriceHistory> getPriceHistories() { return priceHistories; }
    public void setPriceHistories(List<PriceHistory> priceHistories) { this.priceHistories = priceHistories; }

    public List<SharedMember> getSharedMembers() { return sharedMembers; }
    public void setSharedMembers(List<SharedMember> sharedMembers) { this.sharedMembers = sharedMembers; }

    public List<SubscriptionGoal> getGoals() { return goals; }
    public void setGoals(List<SubscriptionGoal> goals) { this.goals = goals; }
}
