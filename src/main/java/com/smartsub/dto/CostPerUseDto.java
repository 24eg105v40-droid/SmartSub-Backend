package com.smartsub.dto;

public class CostPerUseDto {
    private Long subscriptionId;
    private String serviceName;
    private String category;
    private Double monthlyCost;
    private Integer usageDays;
    private Double costPerDay;
    private String efficiencyCategory; // SUPER_EFFICIENT, GOOD_VALUE, MODERATE, EXPENSIVE_PER_USE, UNUSED
    private String message;

    public CostPerUseDto() {}

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

    public Double getCostPerDay() { return costPerDay; }
    public void setCostPerDay(Double costPerDay) { this.costPerDay = costPerDay; }

    public String getEfficiencyCategory() { return efficiencyCategory; }
    public void setEfficiencyCategory(String efficiencyCategory) { this.efficiencyCategory = efficiencyCategory; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
