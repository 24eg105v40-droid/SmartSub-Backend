package com.smartsub.service;

import com.smartsub.dto.PriceChangeRequest;
import com.smartsub.dto.SubscriptionRequest;
import com.smartsub.dto.SubscriptionResponse;
import com.smartsub.model.PriceHistory;
import com.smartsub.model.Subscription;
import com.smartsub.model.SubscriptionEvent;
import com.smartsub.model.User;
import com.smartsub.repository.PriceHistoryRepository;
import com.smartsub.repository.SubscriptionEventRepository;
import com.smartsub.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private SubscriptionEventRepository subscriptionEventRepository;

    @Autowired
    private UserService userService;

    public List<SubscriptionResponse> getAllUserSubscriptions() {
        User user = userService.getAuthenticatedUser();
        return subscriptionRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SubscriptionResponse> getActiveSubscriptions() {
        User user = userService.getAuthenticatedUser();
        return subscriptionRepository.findByUserAndStatus(user, "ACTIVE").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SubscriptionResponse getSubscriptionById(Long id) {
        User user = userService.getAuthenticatedUser();
        Subscription sub = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with ID: " + id));
        return mapToResponse(sub);
    }

    public Subscription getSubscriptionEntityById(Long id) {
        User user = userService.getAuthenticatedUser();
        return subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with ID: " + id));
    }

    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        User user = userService.getAuthenticatedUser();

        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setServiceName(request.getServiceName());
        sub.setPlanName(request.getPlanName());
        sub.setCost(request.getCost());
        sub.setBillingCycle(request.getBillingCycle().toUpperCase());
        sub.setRenewalDate(request.getRenewalDate());
        sub.setCategory(request.getCategory());
        sub.setUsageDaysPerMonth(request.getUsageDaysPerMonth() != null ? request.getUsageDaysPerMonth() : 0);
        sub.setUsageHoursPerWeek(request.getUsageHoursPerWeek() != null ? request.getUsageHoursPerWeek() : 0.0);
        sub.setValueRating(request.getValueRating() != null ? request.getValueRating() : 3);
        sub.setPersonalReason(request.getPersonalReason());
        sub.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        sub.setIcon(request.getIcon() != null ? request.getIcon() : getCategoryDefaultIcon(request.getCategory()));
        sub.setColor(request.getColor() != null ? request.getColor() : getCategoryDefaultColor(request.getCategory()));
        sub.setCreatedAt(LocalDateTime.now());
        sub.setUpdatedAt(LocalDateTime.now());

        Subscription saved = subscriptionRepository.save(sub);

        // Record Initial Timeline Event
        SubscriptionEvent event = new SubscriptionEvent(
                user,
                saved.getServiceName(),
                "STARTED",
                LocalDate.now(),
                "Subscribed to " + saved.getServiceName() + " (" + saved.getPlanName() + ") at ₹" + saved.getCost() + "/" + saved.getBillingCycle().toLowerCase(),
                saved.getIcon(),
                "#10b981"
        );
        subscriptionEventRepository.save(event);

        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse updateSubscription(Long id, SubscriptionRequest request) {
        User user = userService.getAuthenticatedUser();
        Subscription sub = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with ID: " + id));

        // Track price change if cost is different
        if (request.getCost() != null && !request.getCost().equals(sub.getCost())) {
            PriceHistory history = new PriceHistory(
                    sub,
                    sub.getCost(),
                    request.getCost(),
                    LocalDate.now(),
                    "Price updated via edit"
            );
            priceHistoryRepository.save(history);

            double diff = request.getCost() - sub.getCost();
            String diffText = diff > 0 ? ("increased by ₹" + diff) : ("decreased by ₹" + Math.abs(diff));
            SubscriptionEvent event = new SubscriptionEvent(
                    user,
                    sub.getServiceName(),
                    "PRICE_CHANGED",
                    LocalDate.now(),
                    sub.getServiceName() + " cost " + diffText + " (Now ₹" + request.getCost() + ")",
                    "fas fa-chart-line",
                    diff > 0 ? "#ef4444" : "#10b981"
            );
            subscriptionEventRepository.save(event);
        }

        sub.setServiceName(request.getServiceName());
        sub.setPlanName(request.getPlanName());
        sub.setCost(request.getCost());
        sub.setBillingCycle(request.getBillingCycle().toUpperCase());
        sub.setRenewalDate(request.getRenewalDate());
        sub.setCategory(request.getCategory());
        sub.setUsageDaysPerMonth(request.getUsageDaysPerMonth() != null ? request.getUsageDaysPerMonth() : sub.getUsageDaysPerMonth());
        sub.setUsageHoursPerWeek(request.getUsageHoursPerWeek() != null ? request.getUsageHoursPerWeek() : sub.getUsageHoursPerWeek());
        sub.setValueRating(request.getValueRating() != null ? request.getValueRating() : sub.getValueRating());
        sub.setPersonalReason(request.getPersonalReason());
        if (request.getStatus() != null) sub.setStatus(request.getStatus());
        if (request.getIcon() != null) sub.setIcon(request.getIcon());
        if (request.getColor() != null) sub.setColor(request.getColor());
        sub.setUpdatedAt(LocalDateTime.now());

        Subscription saved = subscriptionRepository.save(sub);
        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse recordPriceChange(Long id, PriceChangeRequest request) {
        User user = userService.getAuthenticatedUser();
        Subscription sub = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with ID: " + id));

        PriceHistory history = new PriceHistory(
                sub,
                sub.getCost(),
                request.getNewPrice(),
                request.getEffectiveDate() != null ? request.getEffectiveDate() : LocalDate.now(),
                request.getReason() != null ? request.getReason() : "Price adjusted"
        );
        priceHistoryRepository.save(history);

        double diff = request.getNewPrice() - sub.getCost();
        String diffText = diff > 0 ? ("increased by ₹" + diff) : ("decreased by ₹" + Math.abs(diff));

        SubscriptionEvent event = new SubscriptionEvent(
                user,
                sub.getServiceName(),
                "PRICE_CHANGED",
                request.getEffectiveDate() != null ? request.getEffectiveDate() : LocalDate.now(),
                sub.getServiceName() + " price " + diffText + ". Reason: " + request.getReason(),
                "fas fa-tags",
                diff > 0 ? "#ef4444" : "#10b981"
        );
        subscriptionEventRepository.save(event);

        sub.setCost(request.getNewPrice());
        sub.setUpdatedAt(LocalDateTime.now());
        Subscription saved = subscriptionRepository.save(sub);

        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse toggleStatus(Long id, String status) {
        User user = userService.getAuthenticatedUser();
        Subscription sub = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with ID: " + id));

        String oldStatus = sub.getStatus();
        sub.setStatus(status.toUpperCase());
        sub.setUpdatedAt(LocalDateTime.now());
        Subscription saved = subscriptionRepository.save(sub);

        SubscriptionEvent event = new SubscriptionEvent(
                user,
                sub.getServiceName(),
                status.equalsIgnoreCase("PAUSED") ? "PAUSED" : (status.equalsIgnoreCase("ACTIVE") ? "RESUMED" : "CANCELLED"),
                LocalDate.now(),
                "Subscription status changed from " + oldStatus + " to " + status.toUpperCase(),
                "fas fa-toggle-on",
                status.equalsIgnoreCase("ACTIVE") ? "#10b981" : "#f59e0b"
        );
        subscriptionEventRepository.save(event);

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteSubscription(Long id) {
        User user = userService.getAuthenticatedUser();
        Subscription sub = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with ID: " + id));

        SubscriptionEvent event = new SubscriptionEvent(
                user,
                sub.getServiceName(),
                "CANCELLED",
                LocalDate.now(),
                "Deleted/Cancelled subscription for " + sub.getServiceName() + " (Saved ₹" + sub.getMonthlyCostEquivalent() + "/mo)",
                "fas fa-trash-alt",
                "#ef4444"
        );
        subscriptionEventRepository.save(event);

        subscriptionRepository.delete(sub);
    }

    public SubscriptionResponse mapToResponse(Subscription sub) {
        SubscriptionResponse dto = new SubscriptionResponse();
        dto.setId(sub.getId());
        dto.setServiceName(sub.getServiceName());
        dto.setPlanName(sub.getPlanName());
        dto.setCost(sub.getCost());
        dto.setBillingCycle(sub.getBillingCycle());
        dto.setRenewalDate(sub.getRenewalDate());
        dto.setCategory(sub.getCategory());
        dto.setUsageDaysPerMonth(sub.getUsageDaysPerMonth());
        dto.setUsageHoursPerWeek(sub.getUsageHoursPerWeek());
        dto.setValueRating(sub.getValueRating());
        dto.setPersonalReason(sub.getPersonalReason());
        dto.setStatus(sub.getStatus());
        dto.setIcon(sub.getIcon());
        dto.setColor(sub.getColor());
        dto.setCreatedAt(sub.getCreatedAt());
        dto.setUpdatedAt(sub.getUpdatedAt());

        // Derived calculations
        double monthlyCost = sub.getMonthlyCostEquivalent();
        dto.setMonthlyCostEquivalent(monthlyCost);
        dto.setYearlyCostEquivalent(sub.getYearlyCostEquivalent());

        // Days until renewal
        LocalDate now = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(now, sub.getRenewalDate());
        dto.setDaysUntilRenewal(daysUntil);

        // Usage Category
        int days = sub.getUsageDaysPerMonth() != null ? sub.getUsageDaysPerMonth() : 0;
        if (days <= 5) {
            dto.setUsageCategory("LOW_USAGE");
        } else if (days <= 15) {
            dto.setUsageCategory("OPTIMAL");
        } else {
            dto.setUsageCategory("HIGH_USAGE");
        }

        // Cost Per Use
        double costPerUse = (days > 0) ? (monthlyCost / days) : monthlyCost;
        dto.setCostPerUse(Math.round(costPerUse * 100.0) / 100.0);

        // Subscription Health Score (0 - 100)
        // Factors: Usage Days (weight 40%), Value Rating (weight 30%), Cost Impact (weight 30%)
        int usageScore = Math.min(100, (int) ((days / 30.0) * 100));
        int ratingScore = (sub.getValueRating() != null ? sub.getValueRating() : 3) * 20;
        int costScore = monthlyCost <= 300 ? 95 : (monthlyCost <= 800 ? 75 : (monthlyCost <= 1500 ? 55 : 35));
        int healthScore = (int) (0.4 * usageScore + 0.3 * ratingScore + 0.3 * costScore);
        dto.setHealthScore(healthScore);

        if (healthScore >= 75) {
            dto.setHealthScoreStatus("EXCELLENT");
        } else if (healthScore >= 55) {
            dto.setHealthScoreStatus("GOOD");
        } else if (healthScore >= 35) {
            dto.setHealthScoreStatus("ATTENTION_NEEDED");
        } else {
            dto.setHealthScoreStatus("POOR");
        }

        // Renewal Risk Score
        // High Attention if: Renewing in <= 7 days AND (usage is low OR health score is poor OR cost > 500 with low usage)
        if (daysUntil >= 0 && daysUntil <= 3 && (days <= 6 || healthScore < 50)) {
            dto.setRenewalRiskLevel("HIGH_ATTENTION");
            dto.setRenewalRiskReason("Renews in " + daysUntil + " days with low usage (" + days + " days/mo) and low health score (" + healthScore + "/100)");
        } else if (daysUntil >= 0 && daysUntil <= 7) {
            if (days <= 8) {
                dto.setRenewalRiskLevel("HIGH_ATTENTION");
                dto.setRenewalRiskReason("Upcoming renewal in " + daysUntil + " days with low recorded usage");
            } else {
                dto.setRenewalRiskLevel("MEDIUM_RISK");
                dto.setRenewalRiskReason("Renews in " + daysUntil + " days. Review plan before automatic charge.");
            }
        } else if (days <= 3) {
            dto.setRenewalRiskLevel("MEDIUM_RISK");
            dto.setRenewalRiskReason("Very low monthly usage recorded (" + days + " days/mo)");
        } else {
            dto.setRenewalRiskLevel("LOW_RISK");
            dto.setRenewalRiskReason("Healthy usage and active value received");
        }

        dto.setPriceHistories(sub.getPriceHistories());
        dto.setSharedMembers(sub.getSharedMembers());
        dto.setGoals(sub.getGoals());

        return dto;
    }

    private String getCategoryDefaultIcon(String category) {
        if (category == null) return "fas fa-box";
        switch (category.toLowerCase()) {
            case "entertainment": return "fas fa-film";
            case "productivity": return "fas fa-briefcase";
            case "cloud storage":
            case "cloud": return "fas fa-cloud";
            case "fitness":
            case "health": return "fas fa-heartbeat";
            case "education": return "fas fa-graduation-cap";
            case "utilities": return "fas fa-bolt";
            case "gaming": return "fas fa-gamepad";
            case "news": return "fas fa-newspaper";
            default: return "fas fa-layer-group";
        }
    }

    private String getCategoryDefaultColor(String category) {
        if (category == null) return "#6366f1";
        switch (category.toLowerCase()) {
            case "entertainment": return "#e11d48";
            case "productivity": return "#3b82f6";
            case "cloud storage":
            case "cloud": return "#0ea5e9";
            case "fitness":
            case "health": return "#10b981";
            case "education": return "#8b5cf6";
            case "utilities": return "#f59e0b";
            case "gaming": return "#ec4899";
            case "news": return "#64748b";
            default: return "#6366f1";
        }
    }
}
