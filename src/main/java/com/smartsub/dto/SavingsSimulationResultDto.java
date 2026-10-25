package com.smartsub.dto;

import java.util.List;

public class SavingsSimulationResultDto {
    private int originalActiveCount;
    private double originalMonthlyCost;
    private double originalYearlyCost;

    private int simulatedCancelledCount;
    private double potentialMonthlySavings;
    private double potentialYearlySavings;

    private double newMonthlyCommitment;
    private double newYearlyCommitment;

    private double monthlyBudget;
    private double originalBudgetRemaining;
    private double newBudgetRemaining;
    private double budgetSavingsPercentage;

    private List<SubscriptionResponse> cancelledSubscriptions;

    public SavingsSimulationResultDto() {}

    public int getOriginalActiveCount() { return originalActiveCount; }
    public void setOriginalActiveCount(int originalActiveCount) { this.originalActiveCount = originalActiveCount; }

    public double getOriginalMonthlyCost() { return originalMonthlyCost; }
    public void setOriginalMonthlyCost(double originalMonthlyCost) { this.originalMonthlyCost = originalMonthlyCost; }

    public double getOriginalYearlyCost() { return originalYearlyCost; }
    public void setOriginalYearlyCost(double originalYearlyCost) { this.originalYearlyCost = originalYearlyCost; }

    public int getSimulatedCancelledCount() { return simulatedCancelledCount; }
    public void setSimulatedCancelledCount(int simulatedCancelledCount) { this.simulatedCancelledCount = simulatedCancelledCount; }

    public double getPotentialMonthlySavings() { return potentialMonthlySavings; }
    public void setPotentialMonthlySavings(double potentialMonthlySavings) { this.potentialMonthlySavings = potentialMonthlySavings; }

    public double getPotentialYearlySavings() { return potentialYearlySavings; }
    public void setPotentialYearlySavings(double potentialYearlySavings) { this.potentialYearlySavings = potentialYearlySavings; }

    public double getNewMonthlyCommitment() { return newMonthlyCommitment; }
    public void setNewMonthlyCommitment(double newMonthlyCommitment) { this.newMonthlyCommitment = newMonthlyCommitment; }

    public double getNewYearlyCommitment() { return newYearlyCommitment; }
    public void setNewYearlyCommitment(double newYearlyCommitment) { this.newYearlyCommitment = newYearlyCommitment; }

    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public double getOriginalBudgetRemaining() { return originalBudgetRemaining; }
    public void setOriginalBudgetRemaining(double originalBudgetRemaining) { this.originalBudgetRemaining = originalBudgetRemaining; }

    public double getNewBudgetRemaining() { return newBudgetRemaining; }
    public void setNewBudgetRemaining(double newBudgetRemaining) { this.newBudgetRemaining = newBudgetRemaining; }

    public double getBudgetSavingsPercentage() { return budgetSavingsPercentage; }
    public void setBudgetSavingsPercentage(double budgetSavingsPercentage) { this.budgetSavingsPercentage = budgetSavingsPercentage; }

    public List<SubscriptionResponse> getCancelledSubscriptions() { return cancelledSubscriptions; }
    public void setCancelledSubscriptions(List<SubscriptionResponse> cancelledSubscriptions) { this.cancelledSubscriptions = cancelledSubscriptions; }
}
