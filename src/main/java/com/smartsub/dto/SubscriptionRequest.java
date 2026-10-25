package com.smartsub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class SubscriptionRequest {

    @NotBlank(message = "Service name is required")
    private String serviceName;

    private String planName;

    @NotNull(message = "Cost is required")
    private Double cost;

    @NotBlank(message = "Billing cycle is required")
    private String billingCycle; // MONTHLY, YEARLY, QUARTERLY, WEEKLY

    @NotNull(message = "Renewal date is required")
    private LocalDate renewalDate;

    @NotBlank(message = "Category is required")
    private String category;

    private Integer usageDaysPerMonth = 0;
    private Double usageHoursPerWeek = 0.0;
    private Integer valueRating = 3;
    private String personalReason;
    private String status = "ACTIVE";
    private String icon = "fas fa-box";
    private String color = "#6366f1";

    public SubscriptionRequest() {}

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
}
