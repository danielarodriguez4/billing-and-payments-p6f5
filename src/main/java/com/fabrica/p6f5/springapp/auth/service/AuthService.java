package com.fabrica.p6f5.springapp.auth.service;

import com.fabrica.p6f5.springapp.dto.AuthResponse;
import com.fabrica.p6f5.springapp.dto.LoginRequest;
import com.fabrica.p6f5.springapp.dto.RegisterRequest;
import com.fabrica.p6f5.springapp.user.model.User;
import com.fabrica.p6f5.springapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authentication Service following Single Responsibility Principle.
 * This service is responsible only for authentication operations.
 */
@Service
public class AuthService {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @Autowired
    public AuthService(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    
    /**
     * Register a new user.
     * 
     * @param request the registration request
     * @return AuthResponse with token and user details
     * @throws RuntimeException if user already exists
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userService.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Create new user
        User user = request.toUser();
        User savedUser = userService.save(user);
        
        // Generate JWT token
        String token = jwtService.generateToken(savedUser);
        
        return new AuthResponse(
                token,
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getId()
        );
    }
    
    /**
     * Authenticate user and return JWT token.
     * 
     * @param request the login request
     * @return AuthResponse with token and user details
     * @throws RuntimeException if authentication fails
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );
        
        // Get user details
        User user = (User) authentication.getPrincipal();
        
        // Generate JWT token
        String token = jwtService.generateToken(user);
        
        return new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getId()
        );
    }
    
    /**
     * Get current authenticated user.
     * 
     * @return Optional containing the current user
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }
        return Optional.empty();
    }
    
    /**
     * Validate JWT token.
     * 
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userService.loadUserByUsername(username);
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }
}
