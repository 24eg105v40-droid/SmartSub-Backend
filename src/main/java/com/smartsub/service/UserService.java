package com.smartsub.service;

import com.smartsub.config.JwtUtils;
import com.smartsub.dto.AuthRequest;
import com.smartsub.dto.AuthResponse;
import com.smartsub.dto.RegisterRequest;
import com.smartsub.dto.UserProfileDto;
import com.smartsub.model.User;
import com.smartsub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName() != null && !request.getFullName().isBlank() ? request.getFullName() : request.getUsername(),
                request.getMonthlyBudget() != null ? request.getMonthlyBudget() : 3000.0,
                request.getCurrency() != null ? request.getCurrency() : "INR"
        );

        User savedUser = userRepository.save(user);

        String token = jwtUtils.generateTokenFromUsername(savedUser.getUsername());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getMonthlyBudget(),
                savedUser.getCurrency()
        );
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUsername()));

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getMonthlyBudget(),
                user.getCurrency()
        );
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));
    }

    public UserProfileDto getProfile() {
        User user = getAuthenticatedUser();
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getMonthlyBudget(),
                user.getCurrency()
        );
    }

    public UserProfileDto updateBudget(Double newBudget) {
        User user = getAuthenticatedUser();
        if (newBudget != null && newBudget >= 0) {
            user.setMonthlyBudget(newBudget);
            userRepository.save(user);
        }
        return getProfile();
    }
}
