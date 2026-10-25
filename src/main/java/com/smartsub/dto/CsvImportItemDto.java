package com.smartsub.dto;

import java.time.LocalDate;

public class CsvImportItemDto {
    private String rawDescription;
    private String detectedServiceName;
    private Double detectedAmount;
    private String suggestedCategory;
    private String suggestedBillingCycle;
    private LocalDate detectedDate;
    private int occurrenceCount;
    private boolean isRecurringCandidate;
    private String reason;

    public CsvImportItemDto() {}

    public CsvImportItemDto(String rawDescription, String detectedServiceName, Double detectedAmount,
                            String suggestedCategory, String suggestedBillingCycle, LocalDate detectedDate,
                            int occurrenceCount, boolean isRecurringCandidate, String reason) {
        this.rawDescription = rawDescription;
        this.detectedServiceName = detectedServiceName;
        this.detectedAmount = detectedAmount;
        this.suggestedCategory = suggestedCategory;
        this.suggestedBillingCycle = suggestedBillingCycle;
        this.detectedDate = detectedDate;
        this.occurrenceCount = occurrenceCount;
        this.isRecurringCandidate = isRecurringCandidate;
        this.reason = reason;
    }

    public String getRawDescription() { return rawDescription; }
    public void setRawDescription(String rawDescription) { this.rawDescription = rawDescription; }

    public String getDetectedServiceName() { return detectedServiceName; }
    public void setDetectedServiceName(String detectedServiceName) { this.detectedServiceName = detectedServiceName; }

    public Double getDetectedAmount() { return detectedAmount; }
    public void setDetectedAmount(Double detectedAmount) { this.detectedAmount = detectedAmount; }

    public String getSuggestedCategory() { return suggestedCategory; }
    public void setSuggestedCategory(String suggestedCategory) { this.suggestedCategory = suggestedCategory; }

    public String getSuggestedBillingCycle() { return suggestedBillingCycle; }
    public void setSuggestedBillingCycle(String suggestedBillingCycle) { this.suggestedBillingCycle = suggestedBillingCycle; }

    public LocalDate getDetectedDate() { return detectedDate; }
    public void setDetectedDate(LocalDate detectedDate) { this.detectedDate = detectedDate; }

    public int getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }

    public boolean isRecurringCandidate() { return isRecurringCandidate; }
    public void setRecurringCandidate(boolean recurringCandidate) { isRecurringCandidate = recurringCandidate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
