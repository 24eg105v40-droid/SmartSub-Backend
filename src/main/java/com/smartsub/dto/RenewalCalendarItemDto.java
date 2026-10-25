package com.smartsub.dto;

import java.time.LocalDate;

public class RenewalCalendarItemDto {

    private Long subscriptionId;
    private String serviceName;
    private String planName;
    private Double cost;
    private String billingCycle;
    private Double monthlyCostEquivalent;
    private LocalDate renewalDate;
    private long daysUntilRenewal;
    private String category;
    private String color;
    private String icon;
    private boolean isWithin7Days;

    public RenewalCalendarItemDto() {}

    public RenewalCalendarItemDto(Long subscriptionId, String serviceName, String planName, Double cost,
                                  String billingCycle, Double monthlyCostEquivalent, LocalDate renewalDate,
                                  long daysUntilRenewal, String category, String color, String icon, boolean isWithin7Days) {
        this.subscriptionId = subscriptionId;
        this.serviceName = serviceName;
        this.planName = planName;
        this.cost = cost;
        this.billingCycle = billingCycle;
        this.monthlyCostEquivalent = monthlyCostEquivalent;
        this.renewalDate = renewalDate;
        this.daysUntilRenewal = daysUntilRenewal;
        this.category = category;
        this.color = color;
        this.icon = icon;
        this.isWithin7Days = isWithin7Days;
    }

    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public Double getMonthlyCostEquivalent() { return monthlyCostEquivalent; }
    public void setMonthlyCostEquivalent(Double monthlyCostEquivalent) { this.monthlyCostEquivalent = monthlyCostEquivalent; }

    public LocalDate getRenewalDate() { return renewalDate; }
    public void setRenewalDate(LocalDate renewalDate) { this.renewalDate = renewalDate; }

    public long getDaysUntilRenewal() { return daysUntilRenewal; }
    public void setDaysUntilRenewal(long daysUntilRenewal) { this.daysUntilRenewal = daysUntilRenewal; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isWithin7Days() { return isWithin7Days; }
    public void setWithin7Days(boolean within7Days) { isWithin7Days = within7Days; }
}
