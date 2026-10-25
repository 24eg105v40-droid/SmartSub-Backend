package com.smartsub.dto;

import java.util.ArrayList;
import java.util.List;

public class AiAdvisorResponseDto {

    private String answer;
    private Double estimatedSavingsMonthly;
    private Double estimatedSavingsYearly;
    private List<String> actionItems = new ArrayList<>();
    private boolean configured;
    private String setupNotice;
    private String disclaimer = "SmartSub AI provides suggestions, not guaranteed financial results. Subscriptions are not automatically cancelled.";

    public AiAdvisorResponseDto() {}

    public AiAdvisorResponseDto(String answer, Double estimatedSavingsMonthly, Double estimatedSavingsYearly, List<String> actionItems, boolean configured, String setupNotice) {
        this.answer = answer;
        this.estimatedSavingsMonthly = estimatedSavingsMonthly;
        this.estimatedSavingsYearly = estimatedSavingsYearly;
        this.actionItems = actionItems != null ? actionItems : new ArrayList<>();
        this.configured = configured;
        this.setupNotice = setupNotice;
        this.disclaimer = "SmartSub AI provides suggestions, not guaranteed financial results. Subscriptions are not automatically cancelled.";
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Double getEstimatedSavingsMonthly() { return estimatedSavingsMonthly; }
    public void setEstimatedSavingsMonthly(Double estimatedSavingsMonthly) { this.estimatedSavingsMonthly = estimatedSavingsMonthly; }

    public Double getEstimatedSavingsYearly() { return estimatedSavingsYearly; }
    public void setEstimatedSavingsYearly(Double estimatedSavingsYearly) { this.estimatedSavingsYearly = estimatedSavingsYearly; }

    public List<String> getActionItems() { return actionItems; }
    public void setActionItems(List<String> actionItems) { this.actionItems = actionItems; }

    public boolean isConfigured() { return configured; }
    public void setConfigured(boolean configured) { this.configured = configured; }

    public String getSetupNotice() { return setupNotice; }
    public void setSetupNotice(String setupNotice) { this.setupNotice = setupNotice; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
