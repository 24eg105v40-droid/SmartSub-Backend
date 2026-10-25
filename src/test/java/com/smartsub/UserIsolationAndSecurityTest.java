package com.smartsub;

import com.smartsub.model.SharedMember;
import com.smartsub.model.Subscription;
import com.smartsub.model.User;
import com.smartsub.repository.SharedMemberRepository;
import com.smartsub.repository.SubscriptionRepository;
import com.smartsub.service.SharedSubscriptionService;
import com.smartsub.service.SubscriptionService;
import com.smartsub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserIsolationAndSecurityTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SharedMemberRepository sharedMemberRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SharedSubscriptionService sharedSubscriptionService;

    private User userAlice;
    private User userBob;
    private Subscription aliceSub;
    private SharedMember aliceMember;

    @BeforeEach
    void setUp() {
        userAlice = new User("alice", "alice@example.com", "passA", "Alice", 3000.0, "INR");
        userAlice.setId(1L);

        userBob = new User("bob", "bob@example.com", "passB", "Bob", 3000.0, "INR");
        userBob.setId(2L);

        aliceSub = new Subscription();
        aliceSub.setId(10L);
        aliceSub.setUser(userAlice);
        aliceSub.setServiceName("Alice Netflix");
        aliceSub.setCost(649.0);
        aliceSub.setBillingCycle("MONTHLY");
        aliceSub.setRenewalDate(LocalDate.now().plusDays(10));
        aliceSub.setCategory("Entertainment");

        aliceMember = new SharedMember(aliceSub, "Friend 1", "friend@example.com", 162.25, false, "Note");
        aliceMember.setId(100L);
    }

    @Test
    void testBobCannotAccessAliceSubscription() {
        // Bob is logged in
        when(userService.getAuthenticatedUser()).thenReturn(userBob);
        when(subscriptionRepository.findByIdAndUser(10L, userBob)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.getSubscriptionById(10L);
        });
    }

    @Test
    void testBobCannotTogglePaymentForAliceMember() {
        // Bob is logged in
        when(userService.getAuthenticatedUser()).thenReturn(userBob);
        when(sharedMemberRepository.findByIdAndSubscription_User(100L, userBob)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            sharedSubscriptionService.togglePaymentStatus(100L, true);
        });
    }

    @Test
    void testBobCannotDeleteAliceMember() {
        // Bob is logged in
        when(userService.getAuthenticatedUser()).thenReturn(userBob);
        when(sharedMemberRepository.findByIdAndSubscription_User(100L, userBob)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            sharedSubscriptionService.removeMember(100L);
        });
    }

    @Test
    void testAliceCanAccessHerOwnSubscription() {
        when(userService.getAuthenticatedUser()).thenReturn(userAlice);
        when(subscriptionRepository.findByIdAndUser(10L, userAlice)).thenReturn(Optional.of(aliceSub));

        assertNotNull(subscriptionService.getSubscriptionById(10L));
    }
}
