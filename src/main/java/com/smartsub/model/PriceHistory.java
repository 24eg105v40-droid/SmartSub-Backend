package com.smartsub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "price_histories")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    @JsonIgnore
    private Subscription subscription;

    private Double oldPrice;
    private Double newPrice;
    private Double difference;
    private LocalDate changeDate;
    private String changeReason;

    public PriceHistory() {}

    public PriceHistory(Subscription subscription, Double oldPrice, Double newPrice, LocalDate changeDate, String changeReason) {
        this.subscription = subscription;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.difference = Math.round((newPrice - oldPrice) * 100.0) / 100.0;
        this.changeDate = changeDate != null ? changeDate : LocalDate.now();
        this.changeReason = changeReason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Subscription getSubscription() { return subscription; }
    public void setSubscription(Subscription subscription) { this.subscription = subscription; }

    public Double getOldPrice() { return oldPrice; }
    public void setOldPrice(Double oldPrice) { this.oldPrice = oldPrice; }

    public Double getNewPrice() { return newPrice; }
    public void setNewPrice(Double newPrice) { this.newPrice = newPrice; }

    public Double getDifference() { return difference; }
    public void setDifference(Double difference) { this.difference = difference; }

    public LocalDate getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDate changeDate) { this.changeDate = changeDate; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
}
