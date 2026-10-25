package com.smartsub.dto;

import jakarta.validation.constraints.NotBlank;

public class AiAdvisorRequestDto {

    @NotBlank(message = "Query or question cannot be empty")
    private String query;

    public AiAdvisorRequestDto() {}

    public AiAdvisorRequestDto(String query) {
        this.query = query;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}
