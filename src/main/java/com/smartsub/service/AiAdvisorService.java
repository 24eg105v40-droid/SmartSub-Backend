package com.smartsub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsub.dto.AiAdvisorRequestDto;
import com.smartsub.dto.AiAdvisorResponseDto;
import com.smartsub.dto.DashboardSummaryDto;
import com.smartsub.dto.OverlapGroupDto;
import com.smartsub.model.Subscription;
import com.smartsub.model.User;
import com.smartsub.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAdvisorService {

    private static final Logger logger = LoggerFactory.getLogger(AiAdvisorService.class);

    @Value("${app.openai.api-key:}")
    private String configuredApiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String modelName;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private IntelligenceAnalyticsService analyticsService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiAdvisorResponseDto getAdvice(AiAdvisorRequestDto request) {
        User user = userService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");
        DashboardSummaryDto summary = analyticsService.getDashboardSummary();

        String apiKey = getEffectiveApiKey();

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                return callOpenAiAdvisor(user, activeSubs, summary, request.getQuery(), apiKey);
            } catch (Exception e) {
                logger.warn("OpenAI API call failed: {}. Falling back to Rule-Based Subscription Intelligence.", e.getMessage());
                AiAdvisorResponseDto fallback = generateRuleBasedAdvice(user, activeSubs, summary, request.getQuery());
                fallback.setConfigured(false);
                fallback.setSetupNotice("Notice: OpenAI API call encountered an error (" + e.getMessage() + "). Showing SmartSub Intelligence fallback advice.");
                return fallback;
            }
        }

        // Rule-based engine when API key is not configured
        AiAdvisorResponseDto response = generateRuleBasedAdvice(user, activeSubs, summary, request.getQuery());
        response.setConfigured(false);
        response.setSetupNotice("OpenAI API key is not configured in backend environment variables (OPENAI_API_KEY). Answers are generated deterministically from your actual database subscription metrics.");
        return response;
    }

    private String getEffectiveApiKey() {
        String envKey = System.getenv("OPENAI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        return (configuredApiKey != null && !configuredApiKey.isBlank()) ? configuredApiKey.trim() : null;
    }

    private AiAdvisorResponseDto callOpenAiAdvisor(User user, List<Subscription> activeSubs,
                                                  DashboardSummaryDto summary, String userQuery, String apiKey) throws Exception {
        // Build actual subscription portfolio context for LLM
        StringBuilder portfolioContext = new StringBuilder();
        portfolioContext.append("User Name: ").append(user.getFullName()).append("\n");
        portfolioContext.append("Monthly Budget: ₹").append(summary.getMonthlyBudget()).append("\n");
        portfolioContext.append("Total Monthly Recurring: ₹").append(summary.getTotalMonthlyCost()).append("\n");
        portfolioContext.append("Total Yearly Commitment: ₹").append(summary.getTotalYearlyCost()).append("\n");
        portfolioContext.append("Budget Remaining: ₹").append(summary.getBudgetRemaining()).append("\n");
        portfolioContext.append("Active Subscriptions Count: ").append(activeSubs.size()).append("\n\n");

        portfolioContext.append("Active Subscriptions Portfolio:\n");
        LocalDate today = LocalDate.now();
        for (Subscription sub : activeSubs) {
            long daysUntil = sub.getRenewalDate() != null ? ChronoUnit.DAYS.between(today, sub.getRenewalDate()) : -1;
            portfolioContext.append(String.format(
                    "- %s (%s plan): ₹%s/%s (equiv ₹%s/mo), Category: %s, Recorded Usage: %s days/mo, Renewal in %s days (%s)\n",
                    sub.getServiceName(),
                    sub.getPlanName() != null ? sub.getPlanName() : "Standard",
                    sub.getCost(),
                    sub.getBillingCycle(),
                    sub.getMonthlyCostEquivalent(),
                    sub.getCategory(),
                    sub.getUsageDaysPerMonth(),
                    daysUntil >= 0 ? daysUntil : "N/A",
                    sub.getRenewalDate()
            ));
        }

        List<OverlapGroupDto> overlaps = summary.getOverlaps();
        if (overlaps != null && !overlaps.isEmpty()) {
            portfolioContext.append("\nCategory Overlaps Detected:\n");
            for (OverlapGroupDto og : overlaps) {
                portfolioContext.append(String.format(
                        "- Category %s: %s services (Total ₹%s/mo). Potential savings: ₹%s/mo.\n",
                        og.getCategory(), og.getCount(), og.getTotalMonthlyCost(), og.getPotentialMonthlySavings()
                ));
            }
        }

        String systemPrompt = "You are SmartSub AI Subscription Advisor, an expert personal financial intelligence advisor for recurring subscriptions.\n"
                + "Context of user's subscriptions:\n" + portfolioContext
                + "\nInstructions:\n"
                + "1. Use only the user's actual subscription names, costs, renewal dates, and recorded usage days provided above.\n"
                + "2. Never invent subscriptions, usage numbers, or fabricated dates.\n"
                + "3. State recommendations clearly with explicit numerical calculations for any proposed savings.\n"
                + "4. State that recommendations are suggestions, not guaranteed savings, and you cannot cancel subscriptions automatically.\n"
                + "5. Format your response cleanly using markdown bullet points and bold headers.";

        Map<String, Object> messageSystem = Map.of("role", "system", "content", systemPrompt);
        Map<String, Object> messageUser = Map.of("role", "user", "content", userQuery);

        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("model", modelName);
        requestBodyMap.put("messages", List.of(messageSystem, messageUser));
        requestBodyMap.put("temperature", 0.3);
        requestBodyMap.put("max_tokens", 800);

        String jsonPayload = objectMapper.writeValueAsString(requestBodyMap);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() >= 400) {
            throw new RuntimeException("OpenAI API returned HTTP " + httpResponse.statusCode() + ": " + httpResponse.body());
        }

        JsonNode responseNode = objectMapper.readTree(httpResponse.body());
        String answerContent = responseNode.path("choices").get(0).path("message").path("content").asText();

        // Calculate potential savings mentioned
        double potentialMonthlySavings = summary.getPotentialSavingsMonthly();
        List<String> actions = new ArrayList<>();
        if (summary.getLowUsageCount() > 0) {
            actions.add("Review " + summary.getLowUsageCount() + " low-usage subscriptions (<= 5 days/mo)");
        }
        if (summary.getOverlappingCount() > 0) {
            actions.add("Consolidate overlapping category services to save up to ₹" + potentialMonthlySavings + "/mo");
        }
        if (summary.getRenewingSoonCount() > 0) {
            actions.add("Check " + summary.getRenewingSoonCount() + " subscriptions renewing within 7 days");
        }

        return new AiAdvisorResponseDto(
                answerContent,
                potentialMonthlySavings,
                Math.round(potentialMonthlySavings * 12.0 * 100.0) / 100.0,
                actions,
                true,
                null
        );
    }

    private AiAdvisorResponseDto generateRuleBasedAdvice(User user, List<Subscription> activeSubs,
                                                         DashboardSummaryDto summary, String rawQuery) {
        String query = rawQuery != null ? rawQuery.toLowerCase() : "";
        StringBuilder answer = new StringBuilder();
        double potentialSavings = summary.getPotentialSavingsMonthly();
        List<String> actionItems = new ArrayList<>();

        LocalDate today = LocalDate.now();

        if (activeSubs.isEmpty()) {
            answer.append("### 💡 Welcome to SmartSub AI Advisor!\n\n");
            answer.append("You currently have **0 active subscriptions** in your portfolio. ");
            answer.append("To get personalized intelligence and savings advice, add your recurring subscriptions using the **'+ Add Subscription'** button or import a bank statement via **Import & Timeline**.");
            return new AiAdvisorResponseDto(answer.toString(), 0.0, 0.0, List.of("Add your first subscription to begin analysis"), false, null);
        }

        // Scenario 1: How can I save ₹X or cut costs?
        if (query.contains("save") || query.contains("500") || query.contains("reduce") || query.contains("cut") || query.contains("cheaper")) {
            answer.append("### 💰 Potential Savings Plan\n\n");
            answer.append(String.format("Your current recurring spend is **₹%s/month** (₹%s/year) against a budget of **₹%s/month**.\n\n",
                    summary.getTotalMonthlyCost(), summary.getTotalYearlyCost(), summary.getMonthlyBudget()));

            List<Subscription> lowUsage = activeSubs.stream()
                    .filter(s -> s.getUsageDaysPerMonth() != null && s.getUsageDaysPerMonth() <= 5)
                    .sorted(Comparator.comparingDouble(Subscription::getMonthlyCostEquivalent).reversed())
                    .collect(Collectors.toList());

            if (!lowUsage.isEmpty()) {
                answer.append("#### 1. Underutilized Subscriptions (Immediate Savings Opportunity)\n");
                double subSavings = 0.0;
                for (Subscription s : lowUsage) {
                    double monthlyEquiv = s.getMonthlyCostEquivalent();
                    subSavings += monthlyEquiv;
                    answer.append(String.format("- **%s**: ₹%s/month — Recorded usage: **%s days/month** (Cost/use: ₹%.2f/day). Cancelling or pausing this saves **₹%s/year**.\n",
                            s.getServiceName(), monthlyEquiv, s.getUsageDaysPerMonth(),
                            (s.getUsageDaysPerMonth() > 0 ? monthlyEquiv / s.getUsageDaysPerMonth() : monthlyEquiv),
                            Math.round(monthlyEquiv * 12.0)));
                    actionItems.add("Consider pausing " + s.getServiceName() + " to save ₹" + monthlyEquiv + "/mo");
                }
                answer.append(String.format("\n👉 Pausing or cutting these underutilized plans would save **₹%.2f/month** (₹%.2f/year)!\n\n",
                        subSavings, subSavings * 12.0));
                potentialSavings = subSavings;
            }

            if (summary.getOverlaps() != null && !summary.getOverlaps().isEmpty()) {
                answer.append("#### 2. Redundant Category Services\n");
                for (OverlapGroupDto og : summary.getOverlaps()) {
                    answer.append(String.format("- **%s**: You have %s subscriptions totaling ₹%s/month. Consolidating could save up to **₹%s/month** (₹%s/year).\n",
                            og.getCategory(), og.getCount(), og.getTotalMonthlyCost(), og.getPotentialMonthlySavings(), og.getPotentialYearlySavings()));
                    actionItems.add("Consolidate " + og.getCategory() + " subscriptions to save ₹" + og.getPotentialMonthlySavings() + "/mo");
                }
            }

            answer.append("\n*Calculation note: Savings are calculated as sum of monthly-equivalent costs of low-usage plans and secondary category duplicates.*");
        }
        // Scenario 2: Renewals coming up
        else if (query.contains("renewal") || query.contains("renew") || query.contains("upcoming") || query.contains("soon")) {
            answer.append("### 🗓️ Upcoming Subscription Renewals\n\n");
            List<Subscription> sortedRenewals = activeSubs.stream()
                    .filter(s -> s.getRenewalDate() != null)
                    .sorted(Comparator.comparing(Subscription::getRenewalDate))
                    .collect(Collectors.toList());

            if (sortedRenewals.isEmpty()) {
                answer.append("You have no upcoming renewal dates recorded.\n");
            } else {
                answer.append("Here are your upcoming renewal milestones:\n\n");
                for (Subscription s : sortedRenewals) {
                    long days = ChronoUnit.DAYS.between(today, s.getRenewalDate());
                    String statusBadge = days <= 7 ? "🔴 **(Next 7 Days)**" : "🟢";
                    answer.append(String.format("- %s **%s**: Expected charge **₹%s** on **%s** (%s days away)\n",
                            statusBadge, s.getServiceName(), s.getCost(), s.getRenewalDate(), days >= 0 ? days : 0));
                    if (days <= 7) {
                        actionItems.add("Review " + s.getServiceName() + " renewal before " + s.getRenewalDate());
                    }
                }
            }
        }
        // Scenario 3: Low usage or which to review
        else if (query.contains("review") || query.contains("lowest") || query.contains("least") || query.contains("usage") || query.contains("health")) {
            answer.append("### 🔍 Subscriptions Requiring Review\n\n");
            List<Subscription> lowUsage = activeSubs.stream()
                    .filter(s -> s.getUsageDaysPerMonth() != null && s.getUsageDaysPerMonth() <= 8)
                    .sorted(Comparator.comparingInt(s -> s.getUsageDaysPerMonth() != null ? s.getUsageDaysPerMonth() : 0))
                    .collect(Collectors.toList());

            if (lowUsage.isEmpty()) {
                answer.append("Great news! None of your subscriptions currently fall into the low-usage bracket (all used > 8 days/month).\n");
            } else {
                answer.append("The following subscriptions show low engagement relative to their monthly recurring fee:\n\n");
                for (Subscription s : lowUsage) {
                    double monthlyEquiv = s.getMonthlyCostEquivalent();
                    double costPerDay = (s.getUsageDaysPerMonth() != null && s.getUsageDaysPerMonth() > 0)
                            ? monthlyEquiv / s.getUsageDaysPerMonth()
                            : monthlyEquiv;
                    answer.append(String.format("- **%s** (%s): ₹%s/mo, used only **%s days/month** (Effective cost: **₹%.2f/day**). Reason: *%s*\n",
                            s.getServiceName(), s.getCategory(), monthlyEquiv, s.getUsageDaysPerMonth(), costPerDay,
                            s.getPersonalReason() != null ? s.getPersonalReason() : "No reason logged"));
                    actionItems.add("Assess necessity of " + s.getServiceName());
                }
            }
        }
        // General / Overview response
        else {
            answer.append("### 🧠 SmartSub Portfolio Overview & Insights\n\n");
            answer.append(String.format("- **Total Active Subscriptions**: %s plans\n", summary.getTotalActiveSubscriptions()));
            answer.append(String.format("- **Monthly Recurring Spend**: ₹%s / month\n", summary.getTotalMonthlyCost()));
            answer.append(String.format("- **Yearly Projected Spend**: ₹%s / year\n", summary.getTotalYearlyCost()));
            answer.append(String.format("- **Budget Status**: %s (₹%s remaining of ₹%s budget)\n",
                    summary.isOverBudget() ? "⚠️ OVER BUDGET" : "✅ ON TRACK",
                    summary.getBudgetRemaining(), summary.getMonthlyBudget()));
            answer.append(String.format("- **Potential Monthly Savings Identified**: ₹%s/mo\n\n", summary.getPotentialSavingsMonthly()));

            answer.append("#### Quick Recommendations:\n");
            if (summary.getLowUsageCount() > 0) {
                answer.append(String.format("- You have **%s underutilized plans** (used <= 5 days/mo). Consider testing cancellation in the What-If Simulator.\n", summary.getLowUsageCount()));
            }
            if (summary.getOverlappingCount() > 0) {
                answer.append(String.format("- You have **%s redundant category services**. Review overlaps in Entertainment / Productivity.\n", summary.getOverlappingCount()));
            }
            if (summary.getRenewingSoonCount() > 0) {
                answer.append(String.format("- **%s renewals due in the next 7 days**. Check Renewal Calendar to prevent unwanted auto-debits.\n", summary.getRenewingSoonCount()));
            }

            actionItems.add("Test hypothetical cuts in What-If Simulator");
            actionItems.add("Review Renewal Calendar for upcoming debits");
        }

        return new AiAdvisorResponseDto(
                answer.toString(),
                potentialSavings,
                Math.round(potentialSavings * 12.0 * 100.0) / 100.0,
                actionItems,
                false,
                null
        );
    }
}
