package com.smartsub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.smartsub.config.FirebaseConfig;
import com.smartsub.config.JwtUtils;
import com.smartsub.model.User;
import com.smartsub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class FirebaseAuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationService.class);

    @Autowired
    private FirebaseConfig firebaseConfig;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Authenticates a user by verified Firebase ID token or legacy token.
     * Enforces user existence and idempotent profile creation in the database.
     */
    @Transactional
    public User authenticateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Authentication token is missing");
        }

        // 1. If Firebase Admin SDK is initialized, verify cryptographically with Google
        if (firebaseConfig.isFirebaseInitialized()) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                String uid = decodedToken.getUid();
                String email = decodedToken.getEmail();
                String name = decodedToken.getName();

                return syncFirebaseUser(uid, email, name);
            } catch (Exception e) {
                logger.warn("Firebase ID token verification failed: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid or expired Firebase ID token: " + e.getMessage());
            }
        }

        // 2. If Firebase Admin SDK credentials are not configured yet, check if token is a Firebase ID Token structure
        if (looksLikeFirebaseToken(token)) {
            try {
                FirebaseTokenClaims claims = extractUnverifiedClaims(token);
                if (claims.isExpired()) {
                    throw new IllegalArgumentException("Firebase token has expired");
                }
                logger.info("Verifying Firebase user in local development mode for UID: {}", claims.uid);
                return syncFirebaseUser(claims.uid, claims.email, claims.name);
            } catch (IllegalArgumentException iae) {
                throw iae;
            } catch (Exception e) {
                logger.warn("Could not parse Firebase ID token: {}", e.getMessage());
            }
        }

        // 3. Fallback: Check if it's a valid internal JWT token (e.g. for demo user or unit tests)
        if (jwtUtils.validateJwtToken(token)) {
            String username = jwtUtils.getUserNameFromJwtToken(token);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found for token username: " + username));
        }

        throw new IllegalArgumentException("Invalid, expired, or unsupported authentication token");
    }

    @Transactional
    public User syncFirebaseUser(String uid, String email, String name) {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("Firebase UID cannot be empty");
        }

        // Check if user already exists by Firebase UID
        Optional<User> existingUser = userRepository.findByFirebaseUid(uid);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update name or email if changed
            if (name != null && !name.isBlank() && !name.equals(user.getFullName())) {
                user.setFullName(name);
                userRepository.save(user);
            }
            return user;
        }

        // Check if user already exists by email (to link existing account)
        if (email != null && !email.isBlank()) {
            Optional<User> userByEmail = userRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                User user = userByEmail.get();
                user.setFirebaseUid(uid);
                if (name != null && !name.isBlank()) {
                    user.setFullName(name);
                }
                logger.info("Linked existing user '{}' to Firebase UID '{}'", user.getUsername(), uid);
                return userRepository.save(user);
            }
        }

        // Create new user profile for this Firebase UID
        String safeEmail = (email != null && !email.isBlank()) ? email : (uid + "@smartsub.firebase");
        String baseUsername = safeEmail.split("@")[0].replaceAll("[^a-zA-Z0-9_.-]", "_");
        String finalUsername = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + counter++;
        }

        String displayName = (name != null && !name.isBlank()) ? name : finalUsername;
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User newUser = new User(
                finalUsername,
                safeEmail,
                randomPassword,
                displayName,
                3000.0,
                "INR"
        );
        newUser.setFirebaseUid(uid);

        User savedUser = userRepository.save(newUser);
        logger.info("Successfully provisioned new database user profile '{}' for Firebase UID '{}'", savedUser.getUsername(), uid);
        return savedUser;
    }

    private boolean looksLikeFirebaseToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;
        try {
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            return headerJson.contains("RS256") || headerJson.contains("kid");
        } catch (Exception e) {
            return false;
        }
    }

    private FirebaseTokenClaims extractUnverifiedClaims(String token) throws Exception {
        String[] parts = token.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        JsonNode node = objectMapper.readTree(payloadJson);

        String uid = node.has("user_id") ? node.get("user_id").asText() : (node.has("sub") ? node.get("sub").asText() : null);
        String email = node.has("email") ? node.get("email").asText() : null;
        String name = node.has("name") ? node.get("name").asText() : (node.has("display_name") ? node.get("display_name").asText() : null);
        long exp = node.has("exp") ? node.get("exp").asLong() : 0;

        return new FirebaseTokenClaims(uid, email, name, exp);
    }

    private static class FirebaseTokenClaims {
        final String uid;
        final String email;
        final String name;
        final long exp;

        FirebaseTokenClaims(String uid, String email, String name, long exp) {
            this.uid = uid;
            this.email = email;
            this.name = name;
            this.exp = exp;
        }

        boolean isExpired() {
            if (exp <= 0) return false;
            long nowSec = System.currentTimeMillis() / 1000;
            return nowSec > exp;
        }
    }
}
