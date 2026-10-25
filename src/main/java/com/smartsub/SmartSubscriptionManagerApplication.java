package com.smartsub;

import com.smartsub.model.*;
import com.smartsub.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class SmartSubscriptionManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSubscriptionManagerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initSampleData(UserRepository userRepository,
                                            SubscriptionRepository subscriptionRepository,
                                            PriceHistoryRepository priceHistoryRepository,
                                            SubscriptionGoalRepository goalRepository,
                                            SharedMemberRepository sharedMemberRepository,
                                            SubscriptionEventRepository eventRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("demo").isEmpty()) {
                // Create Default Demo User
                User user = new User(
                        "demo",
                        "demo@smartsub.io",
                        passwordEncoder.encode("password123"),
                        "Alex Johnson",
                        3000.0,
                        "INR"
                );
                userRepository.save(user);

                LocalDate today = LocalDate.now();

                // 1. Netflix (High Attention, Price Changed, Shared Members, Overlap in Entertainment)
                Subscription netflix = new Subscription();
                netflix.setUser(user);
                netflix.setServiceName("Netflix");
                netflix.setPlanName("Premium 4K");
                netflix.setCost(649.0);
                netflix.setBillingCycle("MONTHLY");
                netflix.setRenewalDate(today.plusDays(3)); // Renews in 3 days!
                netflix.setCategory("Entertainment");
                netflix.setUsageDaysPerMonth(8); // Cost per use = 649/8 = 81.12/day
                netflix.setUsageHoursPerWeek(6.0);
                netflix.setValueRating(3);
                netflix.setPersonalReason("Movies and series with family");
                netflix.setStatus("ACTIVE");
                netflix.setIcon("fas fa-film");
                netflix.setColor("#e50914");
                subscriptionRepository.save(netflix);

                // Netflix Price History
                PriceHistory phNetflix = new PriceHistory(netflix, 499.0, 649.0, today.minusMonths(6), "Standard Plan Revised to Premium 4K");
                priceHistoryRepository.save(phNetflix);

                // Netflix Shared Members
                sharedMemberRepository.save(new SharedMember(netflix, "Rahul Sharma", "rahul@example.com", 162.25, true, "Paid via UPI"));
                sharedMemberRepository.save(new SharedMember(netflix, "Pooja Verma", "pooja@example.com", 162.25, true, "Paid via GPay"));
                sharedMemberRepository.save(new SharedMember(netflix, "Vikram Patel", "vikram@example.com", 162.25, false, "Pending reminder"));

                // 2. Spotify (Super efficient, High Usage)
                Subscription spotify = new Subscription();
                spotify.setUser(user);
                spotify.setServiceName("Spotify");
                spotify.setPlanName("Individual Premium");
                spotify.setCost(139.0);
                spotify.setBillingCycle("MONTHLY");
                spotify.setRenewalDate(today.plusDays(20));
                spotify.setCategory("Entertainment");
                spotify.setUsageDaysPerMonth(25); // Cost per use = 139/25 = 5.56/day
                spotify.setUsageHoursPerWeek(15.0);
                spotify.setValueRating(5);
                spotify.setPersonalReason("Daily music while working and traveling");
                spotify.setStatus("ACTIVE");
                spotify.setIcon("fas fa-music");
                spotify.setColor("#1db954");
                subscriptionRepository.save(spotify);

                // 3. Amazon Prime (Entertainment Overlap)
                Subscription prime = new Subscription();
                prime.setUser(user);
                prime.setServiceName("Amazon Prime");
                prime.setPlanName("Annual Shopping & Video");
                prime.setCost(1499.0);
                prime.setBillingCycle("YEARLY"); // ~124.91/mo equivalent
                prime.setRenewalDate(today.plusDays(45));
                prime.setCategory("Entertainment");
                prime.setUsageDaysPerMonth(12);
                prime.setUsageHoursPerWeek(4.0);
                prime.setValueRating(4);
                prime.setPersonalReason("Fast delivery and Prime Video originals");
                prime.setStatus("ACTIVE");
                prime.setIcon("fas fa-shopping-bag");
                prime.setColor("#00a8e1");
                subscriptionRepository.save(prime);

                // 4. Disney+ Hotstar (Low Usage, Overlap candidate!)
                Subscription disney = new Subscription();
                disney.setUser(user);
                disney.setServiceName("Disney+ Hotstar");
                disney.setPlanName("Super Plan");
                disney.setCost(149.0);
                disney.setBillingCycle("MONTHLY");
                disney.setRenewalDate(today.plusDays(10));
                disney.setCategory("Entertainment");
                disney.setUsageDaysPerMonth(2); // Low usage!
                disney.setUsageHoursPerWeek(1.0);
                disney.setValueRating(2);
                disney.setPersonalReason("Live cricket match tournaments");
                disney.setStatus("ACTIVE");
                disney.setIcon("fas fa-tv");
                disney.setColor("#113ccf");
                subscriptionRepository.save(disney);

                // 5. Coursera (Goal Attached)
                Subscription coursera = new Subscription();
                coursera.setUser(user);
                coursera.setServiceName("Coursera");
                coursera.setPlanName("Coursera Plus");
                coursera.setCost(599.0);
                coursera.setBillingCycle("MONTHLY");
                coursera.setRenewalDate(today.plusDays(18));
                coursera.setCategory("Education");
                coursera.setUsageDaysPerMonth(16);
                coursera.setUsageHoursPerWeek(8.0);
                coursera.setValueRating(5);
                coursera.setPersonalReason("Full Stack Software Engineering Specialization");
                coursera.setStatus("ACTIVE");
                coursera.setIcon("fas fa-graduation-cap");
                coursera.setColor("#0056d2");
                subscriptionRepository.save(coursera);

                // Goal for Coursera
                SubscriptionGoal goal = new SubscriptionGoal();
                goal.setUser(user);
                goal.setSubscription(coursera);
                goal.setGoalTitle("Complete Java Certification");
                goal.setGoalDescription("Finish Spring Boot, Microservices & JPA Capstone Project");
                goal.setProgressPercentage(80);
                goal.setTargetDate(today.plusDays(25));
                goalRepository.save(goal);

                // 6. Google One (Cloud Storage)
                Subscription googleOne = new Subscription();
                googleOne.setUser(user);
                googleOne.setServiceName("Google One");
                googleOne.setPlanName("200 GB Storage");
                googleOne.setCost(130.0);
                googleOne.setBillingCycle("MONTHLY");
                googleOne.setRenewalDate(today.plusDays(28));
                googleOne.setCategory("Cloud Storage");
                googleOne.setUsageDaysPerMonth(30);
                googleOne.setUsageHoursPerWeek(20.0);
                googleOne.setValueRating(5);
                googleOne.setPersonalReason("Cloud photo backup and drive storage");
                googleOne.setStatus("ACTIVE");
                googleOne.setIcon("fas fa-cloud");
                googleOne.setColor("#ea4335");
                subscriptionRepository.save(googleOne);

                // 7. GitHub Pro (Productivity)
                Subscription github = new Subscription();
                github.setUser(user);
                github.setServiceName("GitHub Pro");
                github.setPlanName("Developer Tier");
                github.setCost(350.0);
                github.setBillingCycle("MONTHLY");
                github.setRenewalDate(today.plusDays(14));
                github.setCategory("Productivity");
                github.setUsageDaysPerMonth(24);
                github.setUsageHoursPerWeek(25.0);
                github.setValueRating(5);
                github.setPersonalReason("Private repos, advanced GitHub Actions & Copilot");
                github.setStatus("ACTIVE");
                github.setIcon("fab fa-github");
                github.setColor("#24292e");
                subscriptionRepository.save(github);

                // 8. Cult.fit Gym (Fitness, High cost & Low usage alert)
                Subscription gym = new Subscription();
                gym.setUser(user);
                gym.setServiceName("Cult.fit Gym");
                gym.setPlanName("Cult Elite Pass");
                gym.setCost(1500.0);
                gym.setBillingCycle("MONTHLY");
                gym.setRenewalDate(today.plusDays(4)); // Renews in 4 days!
                gym.setCategory("Fitness");
                gym.setUsageDaysPerMonth(3); // Low usage! Cost per day = 500/day
                gym.setUsageHoursPerWeek(3.0);
                gym.setValueRating(3);
                gym.setPersonalReason("Daily fitness training and workout");
                gym.setStatus("ACTIVE");
                gym.setIcon("fas fa-dumbbell");
                gym.setColor("#ff3278");
                subscriptionRepository.save(gym);

                // Sample Timeline Events
                eventRepository.save(new SubscriptionEvent(user, "Cult.fit Gym", "RENEWAL_ALERT", today.minusDays(1), "Upcoming renewal for Cult.fit Gym (₹1,500) in 4 days with low usage", "fas fa-exclamation-triangle", "#ef4444"));
                eventRepository.save(new SubscriptionEvent(user, "Netflix", "PRICE_CHANGED", today.minusMonths(2), "Netflix subscription cost increased by ₹150/month (From ₹499 to ₹649)", "fas fa-chart-line", "#ef4444"));
                eventRepository.save(new SubscriptionEvent(user, "Coursera", "GOAL_PROGRESS", today.minusMonths(1), "Coursera goal 'Complete Java Certification' reached 80% progress!", "fas fa-bullseye", "#8b5cf6"));
                eventRepository.save(new SubscriptionEvent(user, "Spotify", "RENEWED", today.minusMonths(1), "Spotify renewed successfully for ₹139/month", "fas fa-sync-alt", "#10b981"));
                eventRepository.save(new SubscriptionEvent(user, "Amazon Prime", "STARTED", today.minusMonths(4), "Subscribed to Amazon Prime Annual plan at ₹1,499/year", "fas fa-plus-circle", "#3b82f6"));

                System.out.println(">>> Demo user and sample subscription portfolio initialized successfully! <<<");
                System.out.println("Username: demo | Password: password123");
            }
        };
    }
}
