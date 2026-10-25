package com.smartsub.dto;

import java.time.LocalDate;

public class ReceiptScanResultDto {

    private String detectedServiceName;
    private String detectedPlanName;
    private Double detectedCost;
    private String detectedBillingCycle;
    private LocalDate detectedRenewalDate;
    private String detectedCategory;
    private String rawTextSnippet;
    private double confidence;
    private String notes;

    public ReceiptScanResultDto() {}

    public ReceiptScanResultDto(String detectedServiceName, String detectedPlanName, Double detectedCost,
                                String detectedBillingCycle, LocalDate detectedRenewalDate, String detectedCategory,
                                String rawTextSnippet, double confidence, String notes) {
        this.detectedServiceName = detectedServiceName;
        this.detectedPlanName = detectedPlanName;
        this.detectedCost = detectedCost;
        this.detectedBillingCycle = detectedBillingCycle;
        this.detectedRenewalDate = detectedRenewalDate;
        this.detectedCategory = detectedCategory;
        this.rawTextSnippet = rawTextSnippet;
        this.confidence = confidence;
        this.notes = notes;
    }

    public String getDetectedServiceName() { return detectedServiceName; }
    public void setDetectedServiceName(String detectedServiceName) { this.detectedServiceName = detectedServiceName; }

    public String getDetectedPlanName() { return detectedPlanName; }
    public void setDetectedPlanName(String detectedPlanName) { this.detectedPlanName = detectedPlanName; }

    public Double getDetectedCost() { return detectedCost; }
    public void setDetectedCost(Double detectedCost) { this.detectedCost = detectedCost; }

    public String getDetectedBillingCycle() { return detectedBillingCycle; }
    public void setDetectedBillingCycle(String detectedBillingCycle) { this.detectedBillingCycle = detectedBillingCycle; }

    public LocalDate getDetectedRenewalDate() { return detectedRenewalDate; }
    public void setDetectedRenewalDate(LocalDate detectedRenewalDate) { this.detectedRenewalDate = detectedRenewalDate; }

    public String getDetectedCategory() { return detectedCategory; }
    public void setDetectedCategory(String detectedCategory) { this.detectedCategory = detectedCategory; }

    public String getRawTextSnippet() { return rawTextSnippet; }
    public void setRawTextSnippet(String rawTextSnippet) { this.rawTextSnippet = rawTextSnippet; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
