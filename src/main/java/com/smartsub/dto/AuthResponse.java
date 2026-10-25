package com.smartsub.dto;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Double monthlyBudget;
    private String currency;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String username, String email, String fullName, Double monthlyBudget, String currency) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.monthlyBudget = monthlyBudget;
        this.currency = currency;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(Double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
