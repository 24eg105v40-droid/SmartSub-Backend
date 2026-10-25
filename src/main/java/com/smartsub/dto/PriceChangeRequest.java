package com.smartsub.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class PriceChangeRequest {
    @NotNull(message = "New price is required")
    private Double newPrice;

    private String reason;
    private LocalDate effectiveDate = LocalDate.now();

    public PriceChangeRequest() {}

    public Double getNewPrice() { return newPrice; }
    public void setNewPrice(Double newPrice) { this.newPrice = newPrice; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}
