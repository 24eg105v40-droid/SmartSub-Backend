package com.smartsub.dto;

import java.util.List;

public class SavingsSimulationRequest {
    private List<Long> candidateCancelIds;

    public SavingsSimulationRequest() {}

    public SavingsSimulationRequest(List<Long> candidateCancelIds) {
        this.candidateCancelIds = candidateCancelIds;
    }

    public List<Long> getCandidateCancelIds() { return candidateCancelIds; }
    public void setCandidateCancelIds(List<Long> candidateCancelIds) { this.candidateCancelIds = candidateCancelIds; }
}
