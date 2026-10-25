package com.smartsub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "shared_members")
public class SharedMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    @JsonIgnore
    private Subscription subscription;

    private String memberName;
    private String memberEmail;
    private Double shareAmount;
    private Boolean isPaid = false;
    private String notes;

    public SharedMember() {}

    public SharedMember(Subscription subscription, String memberName, String memberEmail, Double shareAmount, Boolean isPaid, String notes) {
        this.subscription = subscription;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.shareAmount = shareAmount;
        this.isPaid = isPaid != null ? isPaid : false;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Subscription getSubscription() { return subscription; }
    public void setSubscription(Subscription subscription) { this.subscription = subscription; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getMemberEmail() { return memberEmail; }
    public void setMemberEmail(String memberEmail) { this.memberEmail = memberEmail; }

    public Double getShareAmount() { return shareAmount; }
    public void setShareAmount(Double shareAmount) { this.shareAmount = shareAmount; }

    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
