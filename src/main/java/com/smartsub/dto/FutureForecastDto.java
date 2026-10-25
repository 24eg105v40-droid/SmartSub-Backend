package com.smartsub.dto;

import java.util.List;
import java.util.Map;

public class FutureForecastDto {
    private double next30DaysCost;
    private double next3MonthsCost;
    private double next6MonthsCost;
    private double next12MonthsCost;
    private int next30DaysRenewalsCount;
    private Map<String, Double> monthlyProjectedExpenditure; // e.g. "Oct 2026" -> 2499.0
    private List<SubscriptionResponse> upcomingRenewalsList;

    public FutureForecastDto() {}

    public double getNext30DaysCost() { return next30DaysCost; }
    public void setNext30DaysCost(double next30DaysCost) { this.next30DaysCost = next30DaysCost; }

    public double getNext3MonthsCost() { return next3MonthsCost; }
    public void setNext3MonthsCost(double next3MonthsCost) { this.next3MonthsCost = next3MonthsCost; }

    public double getNext6MonthsCost() { return next6MonthsCost; }
    public void setNext6MonthsCost(double next6MonthsCost) { this.next6MonthsCost = next6MonthsCost; }

    public double getNext12MonthsCost() { return next12MonthsCost; }
    public void setNext12MonthsCost(double next12MonthsCost) { this.next12MonthsCost = next12MonthsCost; }

    public int getNext30DaysRenewalsCount() { return next30DaysRenewalsCount; }
    public void setNext30DaysRenewalsCount(int next30DaysRenewalsCount) { this.next30DaysRenewalsCount = next30DaysRenewalsCount; }

    public Map<String, Double> getMonthlyProjectedExpenditure() { return monthlyProjectedExpenditure; }
    public void setMonthlyProjectedExpenditure(Map<String, Double> monthlyProjectedExpenditure) { this.monthlyProjectedExpenditure = monthlyProjectedExpenditure; }

    public List<SubscriptionResponse> getUpcomingRenewalsList() { return upcomingRenewalsList; }
    public void setUpcomingRenewalsList(List<SubscriptionResponse> upcomingRenewalsList) { this.upcomingRenewalsList = upcomingRenewalsList; }
}
