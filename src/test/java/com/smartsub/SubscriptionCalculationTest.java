package com.smartsub;

import com.smartsub.dto.SavingsSimulationRequest;
import com.smartsub.dto.SavingsSimulationResultDto;
import com.smartsub.model.Subscription;
import com.smartsub.model.User;
import com.smartsub.repository.SubscriptionRepository;
import com.smartsub.service.IntelligenceAnalyticsService;
import com.smartsub.service.SubscriptionService;
import com.smartsub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionCalculationTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private IntelligenceAnalyticsService analyticsService;

    private User testUser;
    private Subscription subMonthly;
    private Subscription subYearly;
    private Subscription subQuarterly;
    private Subscription subWeekly;

    @BeforeEach
    void setUp() {
        testUser = new User("tester", "tester@smartsub.io", "pass123", "Tester User", 3000.0, "INR");
        testUser.setId(100L);

        subMonthly = new Subscription();
        subMonthly.setId(1L);
        subMonthly.setUser(testUser);
        subMonthly.setServiceName("Netflix");
        subMonthly.setCost(600.0);
        subMonthly.setBillingCycle("MONTHLY");
        subMonthly.setRenewalDate(LocalDate.now().plusDays(5));
        subMonthly.setCategory("Entertainment");
        subMonthly.setUsageDaysPerMonth(10);
        subMonthly.setStatus("ACTIVE");

        subYearly = new Subscription();
        subYearly.setId(2L);
        subYearly.setUser(testUser);
        subYearly.setServiceName("Amazon Prime");
        subYearly.setCost(1200.0);
        subYearly.setBillingCycle("YEARLY");
        subYearly.setRenewalDate(LocalDate.now().plusDays(30));
        subYearly.setCategory("Entertainment");
        subYearly.setUsageDaysPerMonth(20);
        subYearly.setStatus("ACTIVE");

        subQuarterly = new Subscription();
        subQuarterly.setId(3L);
        subQuarterly.setUser(testUser);
        subQuarterly.setServiceName("Antivirus");
        subQuarterly.setCost(300.0);
        subQuarterly.setBillingCycle("QUARTERLY");
        subQuarterly.setRenewalDate(LocalDate.now().plusDays(60));
        subQuarterly.setCategory("Utilities");
        subQuarterly.setUsageDaysPerMonth(30);
        subQuarterly.setStatus("ACTIVE");

        subWeekly = new Subscription();
        subWeekly.setId(4L);
        subWeekly.setUser(testUser);
        subWeekly.setServiceName("News Weekly");
        subWeekly.setCost(50.0);
        subWeekly.setBillingCycle("WEEKLY");
        subWeekly.setRenewalDate(LocalDate.now().plusDays(2));
        subWeekly.setCategory("News");
        subWeekly.setUsageDaysPerMonth(4);
        subWeekly.setStatus("ACTIVE");
    }

    @Test
    void testMonthlyCostEquivalents() {
        // Monthly: ₹600 -> ₹600/mo
        assertEquals(600.0, subMonthly.getMonthlyCostEquivalent());

        // Yearly: ₹1200 / 12 -> ₹100/mo
        assertEquals(100.0, subYearly.getMonthlyCostEquivalent());

        // Quarterly: ₹300 / 3 -> ₹100/mo
        assertEquals(100.0, subQuarterly.getMonthlyCostEquivalent());

        // Weekly: ₹50 * 4.33 -> ₹216.50/mo
        assertEquals(216.50, subWeekly.getMonthlyCostEquivalent());
    }

    @Test
    void testYearlyCostEquivalents() {
        // Monthly: 600 * 12 = 7200
        assertEquals(7200.0, subMonthly.getYearlyCostEquivalent());

        // Yearly: 100 * 12 = 1200
        assertEquals(1200.0, subYearly.getYearlyCostEquivalent());
    }

    @Test
    void testSimulateSavingsWithoutDoubleCounting() {
        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(subscriptionRepository.findByUserAndStatus(testUser, "ACTIVE"))
                .thenReturn(List.of(subMonthly, subYearly));

        // Candidate cuts: Netflix (id 1)
        SavingsSimulationRequest request = new SavingsSimulationRequest();
        request.setCandidateCancelIds(List.of(1L, 1L)); // duplicate ID sent in request

        SavingsSimulationResultDto result = analyticsService.simulateSavings(request);

        // Original monthly: 600 (Netflix) + 100 (Prime) = 700
        assertEquals(700.0, result.getOriginalMonthlyCost());

        // Simulated cuts should deduplicate id 1: Netflix alone = 600 savings
        assertEquals(1, result.getSimulatedCancelledCount());
        assertEquals(600.0, result.getPotentialMonthlySavings());
        assertEquals(7200.0, result.getPotentialYearlySavings());

        // New monthly spend: 700 - 600 = 100
        assertEquals(100.0, result.getNewMonthlyCommitment());

        // Budget remaining: 3000 - 100 = 2900
        assertEquals(2900.0, result.getNewBudgetRemaining());
    }
}
