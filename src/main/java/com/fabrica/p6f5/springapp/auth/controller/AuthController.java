package com.fabrica.p6f5.springapp.auth.controller;

import com.fabrica.p6f5.springapp.common.dto.ApiResponse;
import com.fabrica.p6f5.springapp.auth.dto.AuthResponse;
import com.fabrica.p6f5.springapp.auth.dto.LoginRequest;
import com.fabrica.p6f5.springapp.auth.dto.RegisterRequest;
import com.fabrica.p6f5.springapp.user.model.User;
import com.fabrica.p6f5.springapp.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Authentication Controller following Open/Closed Principle.
 * This controller is open for extension but closed for modification.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     * 
     * @param request the registration request
     * @return ResponseEntity with AuthResponse
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }
    
    /**
     * Authenticate user and return JWT token.
     * 
     * @param request the login request
     * @return ResponseEntity with AuthResponse
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get current user profile.
     * 
     * @return ResponseEntity with current user details
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile() {
        try {
            Optional<User> currentUser = authService.getCurrentUser();
            if (currentUser.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", currentUser.get()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User not authenticated"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve profile: " + e.getMessage()));
        }
    }
    
    /**
     * Validate JWT token.
     * 
     * @param token the JWT token
     * @return ResponseEntity with validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        try {
            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(ApiResponse.success("Token validation completed", isValid));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Token validation failed: " + e.getMessage()));
        }
    }
}
