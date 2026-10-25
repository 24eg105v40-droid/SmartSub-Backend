package com.smartsub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SharedMemberRequest {
    @NotNull(message = "Subscription ID is required")
    private Long subscriptionId;

    @NotBlank(message = "Member name is required")
    private String memberName;

    private String memberEmail;

    @NotNull(message = "Share amount is required")
    private Double shareAmount;

    private Boolean isPaid = false;
    private String notes;

    public SharedMemberRequest() {}

    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

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
