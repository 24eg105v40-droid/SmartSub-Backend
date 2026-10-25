package com.smartsub.service;

import com.smartsub.dto.*;
import com.smartsub.model.Subscription;
import com.smartsub.model.User;
import com.smartsub.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IntelligenceAnalyticsService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    public DashboardSummaryDto getDashboardSummary() {
        User user = userService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setTotalActiveSubscriptions(activeSubs.size());

        double totalMonthly = activeSubs.stream()
                .mapToDouble(Subscription::getMonthlyCostEquivalent)
                .sum();
        totalMonthly = Math.round(totalMonthly * 100.0) / 100.0;
        summary.setTotalMonthlyCost(totalMonthly);
        summary.setTotalYearlyCost(Math.round((totalMonthly * 12.0) * 100.0) / 100.0);

        // Budget Calculations
        double budget = user.getMonthlyBudget() != null ? user.getMonthlyBudget() : 3000.0;
        summary.setMonthlyBudget(budget);
        double budgetRemaining = Math.round((budget - totalMonthly) * 100.0) / 100.0;
        summary.setBudgetRemaining(budgetRemaining);
        summary.setOverBudget(budgetRemaining < 0);
        summary.setBudgetPercentageUsed(budget > 0 ? Math.min(100.0, Math.round((totalMonthly / budget) * 1000.0) / 10.0) : 100.0);

        // Renewing soon count (<= 7 days)
        LocalDate today = LocalDate.now();
        int renewingSoon = (int) activeSubs.stream()
                .filter(s -> {
                    long days = ChronoUnit.DAYS.between(today, s.getRenewalDate());
                    return days >= 0 && days <= 7;
                })
                .count();
        summary.setRenewingSoonCount(renewingSoon);

        // Low usage count (<= 5 days/month)
        int lowUsage = (int) activeSubs.stream()
                .filter(s -> s.getUsageDaysPerMonth() != null && s.getUsageDaysPerMonth() <= 5)
                .count();
        summary.setLowUsageCount(lowUsage);

        // Overlaps
        List<OverlapGroupDto> overlaps = detectOverlapsInternal(activeSubs);
        summary.setOverlaps(overlaps);
        int overlappingCount = overlaps.stream().mapToInt(OverlapGroupDto::getCount).sum();
        summary.setOverlappingCount(overlappingCount);

        // Potential Savings (from low usage and overlapping secondary subscriptions)
        double potentialMonthlySavings = 0.0;
        for (Subscription sub : activeSubs) {
            if (sub.getUsageDaysPerMonth() != null && sub.getUsageDaysPerMonth() <= 5) {
                potentialMonthlySavings += sub.getMonthlyCostEquivalent();
            }
        }
        potentialMonthlySavings = Math.round(potentialMonthlySavings * 100.0) / 100.0;
        summary.setPotentialSavingsMonthly(potentialMonthlySavings);
        summary.setPotentialSavingsYearly(Math.round((potentialMonthlySavings * 12.0) * 100.0) / 100.0);

        // Category spending map
        Map<String, Double> categorySpending = new LinkedHashMap<>();
        for (Subscription sub : activeSubs) {
            String cat = sub.getCategory() != null ? sub.getCategory() : "Other";
            categorySpending.put(cat, Math.round((categorySpending.getOrDefault(cat, 0.0) + sub.getMonthlyCostEquivalent()) * 100.0) / 100.0);
        }
        summary.setCategorySpending(categorySpending);

        // Highest cost subscriptions
        List<SubscriptionResponse> highestCost = activeSubs.stream()
                .sorted(Comparator.comparingDouble(Subscription::getMonthlyCostEquivalent).reversed())
                .limit(5)
                .map(subscriptionService::mapToResponse)
                .collect(Collectors.toList());
        summary.setHighestCostSubscriptions(highestCost);

        // Low usage list
        List<SubscriptionResponse> lowUsageList = activeSubs.stream()
                .filter(s -> s.getUsageDaysPerMonth() != null && s.getUsageDaysPerMonth() <= 5)
                .map(subscriptionService::mapToResponse)
                .collect(Collectors.toList());
        summary.setLowUsageSubscriptions(lowUsageList);

        // High Risk renewals
        List<RenewalRiskDto> riskRenewals = getRenewalRisks().stream()
                .filter(r -> "HIGH_ATTENTION".equals(r.getRiskLevel()) || "MEDIUM_RISK".equals(r.getRiskLevel()))
                .limit(5)
                .collect(Collectors.toList());
        summary.setHighRiskRenewals(riskRenewals);

        return summary;
    }

    public List<HealthScoreDto> getHealthScores() {
        User user = userService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");

        return activeSubs.stream().map(sub -> {
            SubscriptionResponse resp = subscriptionService.mapToResponse(sub);
            HealthScoreDto dto = new HealthScoreDto();
            dto.setSubscriptionId(sub.getId());
            dto.setServiceName(sub.getServiceName());
            dto.setCategory(sub.getCategory());
            dto.setMonthlyCost(resp.getMonthlyCostEquivalent());
            dto.setUsageDays(sub.getUsageDaysPerMonth());
            dto.setValueRating(sub.getValueRating());
            dto.setScore(resp.getHealthScore());
            dto.setStatus(resp.getHealthScoreStatus());

            if (resp.getHealthScore() >= 75) {
                dto.setBadgeColor("#10b981");
                dto.setFeedback("High utility and great return on investment. Keep active.");
            } else if (resp.getHealthScore() >= 55) {
                dto.setBadgeColor("#3b82f6");
                dto.setFeedback("Moderate utility. Plan is reasonably balanced with usage.");
            } else if (resp.getHealthScore() >= 35) {
                dto.setBadgeColor("#f59e0b");
                dto.setFeedback("Review plan! Cost is elevated relative to recorded usage days.");
            } else {
                dto.setBadgeColor("#ef4444");
                dto.setFeedback("High risk of waste! Rarely used with noticeable monthly cost.");
            }
            return dto;
        }).sorted(Comparator.comparingInt(HealthScoreDto::getScore)).collect(Collectors.toList());
    }

    public List<CostPerUseDto> getCostPerUseAnalysis() {
        User user = userService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");

        return activeSubs.stream().map(sub -> {
            CostPerUseDto dto = new CostPerUseDto();
            dto.setSubscriptionId(sub.getId());
            dto.setServiceName(sub.getServiceName());
            dto.setCategory(sub.getCategory());
            double monthlyCost = sub.getMonthlyCostEquivalent();
            dto.setMonthlyCost(monthlyCost);
            int days = sub.getUsageDaysPerMonth() != null ? sub.getUsageDaysPerMonth() : 0;
            dto.setUsageDays(days);

            double costPerDay = (days > 0) ? (monthlyCost / days) : monthlyCost;
            costPerDay = Math.round(costPerDay * 100.0) / 100.0;
            dto.setCostPerDay(costPerDay);

            if (days == 0) {
                dto.setEfficiencyCategory("UNUSED");
                dto.setMessage("0 days used this month! Full cost ₹" + monthlyCost + " unutilized.");
            } else if (costPerDay <= 15.0) {
                dto.setEfficiencyCategory("SUPER_EFFICIENT");
                dto.setMessage("Outstanding value! Only ₹" + costPerDay + " per active day.");
            } else if (costPerDay <= 50.0) {
                dto.setEfficiencyCategory("GOOD_VALUE");
                dto.setMessage("Good value received per usage session.");
            } else if (costPerDay <= 120.0) {
                dto.setEfficiencyCategory("MODERATE");
                dto.setMessage("Moderate cost per use. Consider increasing usage or downgrading tier.");
            } else {
                dto.setEfficiencyCategory("EXPENSIVE_PER_USE");
                dto.setMessage("Expensive at ₹" + costPerDay + "/day! Consider on-demand or pausing.");
            }
            return dto;
        }).sorted(Comparator.comparingDouble(CostPerUseDto::getCostPerDay).reversed()).collect(Collectors.toList());
    }

    public List<OverlapGroupDto> detectOverlaps() {
        User user = userService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");
        return detectOverlapsInternal(activeSubs);
    }

    private List<OverlapGroupDto> detectOverlapsInternal(List<Subscription> activeSubs) {
        Map<String, List<Subscription>> grouped = activeSubs.stream()
                .collect(Collectors.groupingBy(s -> s.getCategory() != null ? s.getCategory() : "Other"));

        List<OverlapGroupDto> overlapGroups = new ArrayList<>();

        for (Map.Entry<String, List<Subscription>> entry : grouped.entrySet()) {
            if (entry.getValue().size() > 1) {
                OverlapGroupDto group = new OverlapGroupDto();
                group.setCategory(entry.getKey());
                group.setCount(entry.getValue().size());

                double totalMonthly = entry.getValue().stream().mapToDouble(Subscription::getMonthlyCostEquivalent).sum();
                totalMonthly = Math.round(totalMonthly * 100.0) / 100.0;
                group.setTotalMonthlyCost(totalMonthly);
                group.setTotalYearlyCost(Math.round((totalMonthly * 12.0) * 100.0) / 100.0);

                // Lowest usage or secondary subscriptions represent potential savings
                List<Subscription> sortedByUsage = entry.getValue().stream()
                        .sorted(Comparator.comparingInt(s -> s.getUsageDaysPerMonth() != null ? s.getUsageDaysPerMonth() : 0))
                        .collect(Collectors.toList());

                // Keep the most used one, the rest are potential overlap savings
                double overlapSavings = 0.0;
                for (int i = 0; i < sortedByUsage.size() - 1; i++) {
                    overlapSavings += sortedByUsage.get(i).getMonthlyCostEquivalent();
                }
                group.setPotentialMonthlySavings(Math.round(overlapSavings * 100.0) / 100.0);
                group.setPotentialYearlySavings(Math.round((overlapSavings * 12.0) * 100.0) / 100.0);

                group.setSubscriptions(entry.getValue().stream().map(subscriptionService::mapToResponse).collect(Collectors.toList()));
                group.setSuggestion("You have " + entry.getValue().size() + " subscriptions in " + entry.getKey() +
                        ". Consolidating could save up to ₹" + group.getPotentialMonthlySavings() + "/month (₹" + group.getPotentialYearlySavings() + "/year).");

                overlapGroups.add(group);
            }
        }

        return overlapGroups;
    }

    public FutureForecastDto getFutureForecast() {
        User user = userService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");

        FutureForecastDto forecast = new FutureForecastDto();
        double monthlyEquivalentSum = activeSubs.stream().mapToDouble(Subscription::getMonthlyCostEquivalent).sum();

        forecast.setNext30DaysCost(Math.round(monthlyEquivalentSum * 100.0) / 100.0);
        forecast.setNext3MonthsCost(Math.round((monthlyEquivalentSum * 3.0) * 100.0) / 100.0);
        forecast.setNext6MonthsCost(Math.round((monthlyEquivalentSum * 6.0) * 100.0) / 100.0);
        forecast.setNext12MonthsCost(Math.round((monthlyEquivalentSum * 12.0) * 100.0) / 100.0);

        LocalDate today = LocalDate.now();
        int renewing30Days = (int) activeSubs.stream()
                .filter(s -> {
                    long days = ChronoUnit.DAYS.between(today, s.getRenewalDate());
                    return days >= 0 && days <= 30;
                })
                .count();
        forecast.setNext30DaysRenewalsCount(renewing30Days);

        // Build 12-month projection breakdown
        Map<String, Double> monthlyProjection = new LinkedHashMap<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 0; i < 12; i++) {
            LocalDate monthDate = today.plusMonths(i);
            String monthLabel = monthDate.format(dtf);
            monthlyProjection.put(monthLabel, Math.round(monthlyEquivalentSum * 100.0) / 100.0);
        }
        forecast.setMonthlyProjectedExpenditure(monthlyProjection);

        // Upcoming renewals sorted by date
        List<SubscriptionResponse> upcoming = activeSubs.stream()
                .sorted(Comparator.comparing(Subscription::getRenewalDate))
                .map(subscriptionService::mapToResponse)
                .collect(Collectors.toList());
        forecast.setUpcomingRenewalsList(upcoming);

        return forecast;
    }

    public List<RenewalRiskDto> getRenewalRisks() {
        User user = userService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");
        LocalDate today = LocalDate.now();

        return activeSubs.stream().map(sub -> {
            SubscriptionResponse resp = subscriptionService.mapToResponse(sub);
            RenewalRiskDto dto = new RenewalRiskDto();
            dto.setSubscriptionId(sub.getId());
            dto.setServiceName(sub.getServiceName());
            dto.setCategory(sub.getCategory());
            dto.setCost(sub.getCost());
            dto.setBillingCycle(sub.getBillingCycle());
            dto.setRenewalDate(sub.getRenewalDate());
            long days = ChronoUnit.DAYS.between(today, sub.getRenewalDate());
            dto.setDaysRemaining(days);
            dto.setUsageDays(sub.getUsageDaysPerMonth() != null ? sub.getUsageDaysPerMonth() : 0);
            dto.setHealthScore(resp.getHealthScore());
            dto.setRiskLevel(resp.getRenewalRiskLevel());

            if ("HIGH_ATTENTION".equals(resp.getRenewalRiskLevel())) {
                dto.setRiskBadgeColor("#ef4444");
                dto.setReason("Upcoming renewal in " + days + " days with low recorded monthly usage.");
                dto.setActionNeeded("🔴 Immediate Review: Consider pausing or downgrading before renewal.");
            } else if ("MEDIUM_RISK".equals(resp.getRenewalRiskLevel())) {
                dto.setRiskBadgeColor("#f59e0b");
                dto.setReason("Renewal approaching in " + days + " days. Moderate health score.");
                dto.setActionNeeded("🟡 Moderate Review: Verify if features are still actively needed.");
            } else {
                dto.setRiskBadgeColor("#10b981");
                dto.setReason("Healthy usage rate and great satisfaction score.");
                dto.setActionNeeded("🟢 Safe: Subscription provides consistent utility.");
            }
            return dto;
        }).sorted((a, b) -> {
            // Sort high attention first, then by days remaining
            int priorityA = "HIGH_ATTENTION".equals(a.getRiskLevel()) ? 0 : ("MEDIUM_RISK".equals(a.getRiskLevel()) ? 1 : 2);
            int priorityB = "HIGH_ATTENTION".equals(b.getRiskLevel()) ? 0 : ("MEDIUM_RISK".equals(b.getRiskLevel()) ? 1 : 2);
            if (priorityA != priorityB) return Integer.compare(priorityA, priorityB);
            return Long.compare(a.getDaysRemaining(), b.getDaysRemaining());
        }).collect(Collectors.toList());
    }

    public SavingsSimulationResultDto simulateSavings(SavingsSimulationRequest request) {
        User user = userService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");

        Set<Long> cancelIds = (request != null && request.getCandidateCancelIds() != null)
                ? new HashSet<>(request.getCandidateCancelIds())
                : Collections.emptySet();

        SavingsSimulationResultDto result = new SavingsSimulationResultDto();
        result.setOriginalActiveCount(activeSubs.size());

        double origMonthly = activeSubs.stream().mapToDouble(Subscription::getMonthlyCostEquivalent).sum();
        result.setOriginalMonthlyCost(Math.round(origMonthly * 100.0) / 100.0);
        result.setOriginalYearlyCost(Math.round((origMonthly * 12.0) * 100.0) / 100.0);

        List<Subscription> cancelled = activeSubs.stream()
                .filter(s -> cancelIds.contains(s.getId()))
                .collect(Collectors.toList());

        result.setSimulatedCancelledCount(cancelled.size());
        double monthlySavings = cancelled.stream().mapToDouble(Subscription::getMonthlyCostEquivalent).sum();
        monthlySavings = Math.round(monthlySavings * 100.0) / 100.0;
        result.setPotentialMonthlySavings(monthlySavings);
        result.setPotentialYearlySavings(Math.round((monthlySavings * 12.0) * 100.0) / 100.0);

        double newMonthly = Math.max(0.0, origMonthly - monthlySavings);
        newMonthly = Math.round(newMonthly * 100.0) / 100.0;
        result.setNewMonthlyCommitment(newMonthly);
        result.setNewYearlyCommitment(Math.round((newMonthly * 12.0) * 100.0) / 100.0);

        double budget = user.getMonthlyBudget() != null ? user.getMonthlyBudget() : 3000.0;
        result.setMonthlyBudget(budget);
        result.setOriginalBudgetRemaining(Math.round((budget - origMonthly) * 100.0) / 100.0);
        result.setNewBudgetRemaining(Math.round((budget - newMonthly) * 100.0) / 100.0);

        double savingsPercentage = origMonthly > 0 ? Math.round((monthlySavings / origMonthly) * 1000.0) / 10.0 : 0.0;
        result.setBudgetSavingsPercentage(savingsPercentage);

        result.setCancelledSubscriptions(cancelled.stream().map(subscriptionService::mapToResponse).collect(Collectors.toList()));

        return result;
    }

    public List<RenewalCalendarItemDto> getRenewalCalendar(String filter) {
        User user = userService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");
        LocalDate today = LocalDate.now();

        return activeSubs.stream()
                .filter(s -> s.getRenewalDate() != null)
                .map(sub -> {
                    long days = ChronoUnit.DAYS.between(today, sub.getRenewalDate());
                    boolean within7Days = days >= 0 && days <= 7;
                    return new RenewalCalendarItemDto(
                            sub.getId(),
                            sub.getServiceName(),
                            sub.getPlanName(),
                            sub.getCost(),
                            sub.getBillingCycle(),
                            sub.getMonthlyCostEquivalent(),
                            sub.getRenewalDate(),
                            days,
                            sub.getCategory(),
                            sub.getColor() != null ? sub.getColor() : "#6366f1",
                            sub.getIcon() != null ? sub.getIcon() : "fas fa-calendar",
                            within7Days
                    );
                })
                .filter(item -> {
                    if ("7days".equalsIgnoreCase(filter) || "week".equalsIgnoreCase(filter)) {
                        return item.getDaysUntilRenewal() >= 0 && item.getDaysUntilRenewal() <= 7;
                    } else if ("30days".equalsIgnoreCase(filter) || "month".equalsIgnoreCase(filter)) {
                        return item.getDaysUntilRenewal() >= 0 && item.getDaysUntilRenewal() <= 30;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(RenewalCalendarItemDto::getRenewalDate))
                .collect(Collectors.toList());
    }
}
