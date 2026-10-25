package com.smartsub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String firebaseUid;

    private String fullName;

    private Double monthlyBudget = 3000.0;

    private String currency = "INR";

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SubscriptionGoal> goals = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SubscriptionEvent> events = new ArrayList<>();

    public User() {}

    public User(String username, String email, String password, String fullName, Double monthlyBudget, String currency) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.monthlyBudget = monthlyBudget != null ? monthlyBudget : 3000.0;
        this.currency = currency != null ? currency : "INR";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(Double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Subscription> getSubscriptions() { return subscriptions; }
    public void setSubscriptions(List<Subscription> subscriptions) { this.subscriptions = subscriptions; }

    public List<SubscriptionGoal> getGoals() { return goals; }
    public void setGoals(List<SubscriptionGoal> goals) { this.goals = goals; }

    public List<SubscriptionEvent> getEvents() { return events; }
    public void setEvents(List<SubscriptionEvent> events) { this.events = events; }
}
