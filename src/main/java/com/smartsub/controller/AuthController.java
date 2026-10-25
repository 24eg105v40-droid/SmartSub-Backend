package com.smartsub.controller;

import com.smartsub.config.JwtUtils;
import com.smartsub.dto.AuthRequest;
import com.smartsub.dto.AuthResponse;
import com.smartsub.dto.RegisterRequest;
import com.smartsub.dto.UserProfileDto;
import com.smartsub.model.User;
import com.smartsub.service.FirebaseAuthenticationService;
import com.smartsub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private FirebaseAuthenticationService firebaseAuthService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            AuthResponse response = userService.register(signUpRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        try {
            AuthResponse response = userService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
        }
    }

    @PostMapping("/firebase-login")
    public ResponseEntity<?> authenticateFirebase(@RequestBody Map<String, String> payload) {
        String idToken = payload.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Firebase idToken is required"));
        }
        try {
            User user = firebaseAuthService.authenticateToken(idToken);
            AuthResponse response = new AuthResponse(
                    idToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getMonthlyBudget(),
                    user.getCurrency()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Authentication failed: " + e.getMessage()));
        }
    }

    @RequestMapping(value = "/demo", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> loginDemo() {
        try {
            AuthRequest demoReq = new AuthRequest("demo", "password123");
            AuthResponse response = userService.login(demoReq);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Demo account unavailable"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    @PutMapping("/budget")
    public ResponseEntity<UserProfileDto> updateBudget(@RequestBody Map<String, Double> payload) {
        Double budget = payload.get("monthlyBudget");
        return ResponseEntity.ok(userService.updateBudget(budget));
    }
}
