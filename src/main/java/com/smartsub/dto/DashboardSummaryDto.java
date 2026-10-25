package com.smartsub.dto;

import java.util.List;
import java.util.Map;

public class DashboardSummaryDto {
    private int totalActiveSubscriptions;
    private double totalMonthlyCost;
    private double totalYearlyCost;
    private int renewingSoonCount; // within 7 days
    private int lowUsageCount;
    private int overlappingCount;
    private double potentialSavingsMonthly;
    private double potentialSavingsYearly;
    private double monthlyBudget;
    private double budgetRemaining;
    private double budgetPercentageUsed;
    private boolean isOverBudget;

    private Map<String, Double> categorySpending;
    private List<SubscriptionResponse> highestCostSubscriptions;
    private List<RenewalRiskDto> highRiskRenewals;
    private List<SubscriptionResponse> lowUsageSubscriptions;
    private List<OverlapGroupDto> overlaps;

    public DashboardSummaryDto() {}

    public int getTotalActiveSubscriptions() { return totalActiveSubscriptions; }
    public void setTotalActiveSubscriptions(int totalActiveSubscriptions) { this.totalActiveSubscriptions = totalActiveSubscriptions; }

    public double getTotalMonthlyCost() { return totalMonthlyCost; }
    public void setTotalMonthlyCost(double totalMonthlyCost) { this.totalMonthlyCost = totalMonthlyCost; }

    public double getTotalYearlyCost() { return totalYearlyCost; }
    public void setTotalYearlyCost(double totalYearlyCost) { this.totalYearlyCost = totalYearlyCost; }

    public int getRenewingSoonCount() { return renewingSoonCount; }
    public void setRenewingSoonCount(int renewingSoonCount) { this.renewingSoonCount = renewingSoonCount; }

    public int getLowUsageCount() { return lowUsageCount; }
    public void setLowUsageCount(int lowUsageCount) { this.lowUsageCount = lowUsageCount; }

    public int getOverlappingCount() { return overlappingCount; }
    public void setOverlappingCount(int overlappingCount) { this.overlappingCount = overlappingCount; }

    public double getPotentialSavingsMonthly() { return potentialSavingsMonthly; }
    public void setPotentialSavingsMonthly(double potentialSavingsMonthly) { this.potentialSavingsMonthly = potentialSavingsMonthly; }

    public double getPotentialSavingsYearly() { return potentialSavingsYearly; }
    public void setPotentialSavingsYearly(double potentialSavingsYearly) { this.potentialSavingsYearly = potentialSavingsYearly; }

    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public double getBudgetRemaining() { return budgetRemaining; }
    public void setBudgetRemaining(double budgetRemaining) { this.budgetRemaining = budgetRemaining; }

    public double getBudgetPercentageUsed() { return budgetPercentageUsed; }
    public void setBudgetPercentageUsed(double budgetPercentageUsed) { this.budgetPercentageUsed = budgetPercentageUsed; }

    public boolean isOverBudget() { return isOverBudget; }
    public void setOverBudget(boolean overBudget) { isOverBudget = overBudget; }

    public Map<String, Double> getCategorySpending() { return categorySpending; }
    public void setCategorySpending(Map<String, Double> categorySpending) { this.categorySpending = categorySpending; }

    public List<SubscriptionResponse> getHighestCostSubscriptions() { return highestCostSubscriptions; }
    public void setHighestCostSubscriptions(List<SubscriptionResponse> highestCostSubscriptions) { this.highestCostSubscriptions = highestCostSubscriptions; }

    public List<RenewalRiskDto> getHighRiskRenewals() { return highRiskRenewals; }
    public void setHighRiskRenewals(List<RenewalRiskDto> highRiskRenewals) { this.highRiskRenewals = highRiskRenewals; }

    public List<SubscriptionResponse> getLowUsageSubscriptions() { return lowUsageSubscriptions; }
    public void setLowUsageSubscriptions(List<SubscriptionResponse> lowUsageSubscriptions) { this.lowUsageSubscriptions = lowUsageSubscriptions; }

    public List<OverlapGroupDto> getOverlaps() { return overlaps; }
    public void setOverlaps(List<OverlapGroupDto> overlaps) { this.overlaps = overlaps; }
}
