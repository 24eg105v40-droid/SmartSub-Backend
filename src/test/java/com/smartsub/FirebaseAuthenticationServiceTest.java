package com.smartsub;

import com.smartsub.config.FirebaseConfig;
import com.smartsub.config.JwtUtils;
import com.smartsub.model.User;
import com.smartsub.repository.UserRepository;
import com.smartsub.service.FirebaseAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FirebaseAuthenticationServiceTest {

    @Mock
    private FirebaseConfig firebaseConfig;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private FirebaseAuthenticationService firebaseAuthService;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
    }

    @Test
    void testSyncFirebaseUserCreatesNewUserIfNotFound() {
        when(userRepository.findByFirebaseUid("firebase-uid-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@smartsub.io")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User synced = firebaseAuthService.syncFirebaseUser("firebase-uid-123", "newuser@smartsub.io", "New User");

        assertNotNull(synced);
        assertEquals("firebase-uid-123", synced.getFirebaseUid());
        assertEquals("newuser@smartsub.io", synced.getEmail());
        assertEquals("New User", synced.getFullName());
        assertEquals(3000.0, synced.getMonthlyBudget());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSyncFirebaseUserLinksExistingUserByEmail() {
        User existing = new User("existing_user", "exist@smartsub.io", "pass", "Existing", 4000.0, "INR");
        existing.setId(55L);

        when(userRepository.findByFirebaseUid("firebase-uid-456")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("exist@smartsub.io")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User synced = firebaseAuthService.syncFirebaseUser("firebase-uid-456", "exist@smartsub.io", "Existing Updated");

        assertNotNull(synced);
        assertEquals("firebase-uid-456", synced.getFirebaseUid());
        assertEquals("Existing Updated", synced.getFullName());
        assertEquals(4000.0, synced.getMonthlyBudget());
        verify(userRepository, times(1)).save(existing);
    }

    @Test
    void testSyncFirebaseUserIdempotentForSameUid() {
        User existing = new User("alex", "alex@smartsub.io", "pass", "Alex Johnson", 3000.0, "INR");
        existing.setFirebaseUid("firebase-uid-789");

        when(userRepository.findByFirebaseUid("firebase-uid-789")).thenReturn(Optional.of(existing));

        User synced = firebaseAuthService.syncFirebaseUser("firebase-uid-789", "alex@smartsub.io", "Alex Johnson");

        assertNotNull(synced);
        assertEquals("firebase-uid-789", synced.getFirebaseUid());
        verify(userRepository, never()).save(any(User.class));
    }
}
