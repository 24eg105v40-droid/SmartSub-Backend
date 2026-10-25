package com.smartsub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String serviceName;

    private String planName;

    @NotNull
    @Column(nullable = false)
    private Double cost;

    @NotBlank
    @Column(nullable = false)
    private String billingCycle; // MONTHLY, YEARLY, QUARTERLY, WEEKLY

    @NotNull
    @Column(nullable = false)
    private LocalDate renewalDate;

    @NotBlank
    @Column(nullable = false)
    private String category; // Entertainment, Productivity, Cloud Storage, Fitness, Education, Utilities, Gaming, News, Other

    private Integer usageDaysPerMonth = 0; // Number of days used in a month (0-30)

    private Double usageHoursPerWeek = 0.0;

    private Integer valueRating = 3; // 1 to 5 stars

    private String personalReason; // e.g. "Movies and series with family"

    private String status = "ACTIVE"; // ACTIVE, PAUSED, CANCELLED

    private String icon = "fas fa-box";

    private String color = "#6366f1";

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceHistory> priceHistories = new ArrayList<>();

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedMember> sharedMembers = new ArrayList<>();

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubscriptionGoal> goals = new ArrayList<>();

    public Subscription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PriceHistory> getPriceHistories() { return priceHistories; }
    public void setPriceHistories(List<PriceHistory> priceHistories) { this.priceHistories = priceHistories; }

    public List<SharedMember> getSharedMembers() { return sharedMembers; }
    public void setSharedMembers(List<SharedMember> sharedMembers) { this.sharedMembers = sharedMembers; }

    public List<SubscriptionGoal> getGoals() { return goals; }
    public void setGoals(List<SubscriptionGoal> goals) { this.goals = goals; }

    /**
     * Normalized Monthly Cost calculation
     */
    @Transient
    public Double getMonthlyCostEquivalent() {
        if (cost == null) return 0.0;
        String cycle = billingCycle != null ? billingCycle.toUpperCase() : "MONTHLY";
        switch (cycle) {
            case "YEARLY":
            case "ANNUAL":
                return Math.round((cost / 12.0) * 100.0) / 100.0;
            case "QUARTERLY":
                return Math.round((cost / 3.0) * 100.0) / 100.0;
            case "WEEKLY":
                return Math.round((cost * 4.33) * 100.0) / 100.0;
            case "MONTHLY":
            default:
                return cost;
        }
    }

    /**
     * Normalized Yearly Cost calculation
     */
    @Transient
    public Double getYearlyCostEquivalent() {
        return Math.round((getMonthlyCostEquivalent() * 12.0) * 100.0) / 100.0;
    }
}
