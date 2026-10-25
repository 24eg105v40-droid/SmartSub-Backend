package com.smartsub.dto;

import java.util.List;

public class OverlapGroupDto {
    private String category;
    private int count;
    private double totalMonthlyCost;
    private double totalYearlyCost;
    private double potentialMonthlySavings;
    private double potentialYearlySavings;
    private List<SubscriptionResponse> subscriptions;
    private String suggestion;

    public OverlapGroupDto() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public double getTotalMonthlyCost() { return totalMonthlyCost; }
    public void setTotalMonthlyCost(double totalMonthlyCost) { this.totalMonthlyCost = totalMonthlyCost; }

    public double getTotalYearlyCost() { return totalYearlyCost; }
    public void setTotalYearlyCost(double totalYearlyCost) { this.totalYearlyCost = totalYearlyCost; }

    public double getPotentialMonthlySavings() { return potentialMonthlySavings; }
    public void setPotentialMonthlySavings(double potentialMonthlySavings) { this.potentialMonthlySavings = potentialMonthlySavings; }

    public double getPotentialYearlySavings() { return potentialYearlySavings; }
    public void setPotentialYearlySavings(double potentialYearlySavings) { this.potentialYearlySavings = potentialYearlySavings; }

    public List<SubscriptionResponse> getSubscriptions() { return subscriptions; }
    public void setSubscriptions(List<SubscriptionResponse> subscriptions) { this.subscriptions = subscriptions; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
}
