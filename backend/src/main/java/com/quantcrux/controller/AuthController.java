package com.quantcrux.controller;

import com.quantcrux.dto.*;
import com.quantcrux.model.User;
import com.quantcrux.model.UserRole;
import com.quantcrux.security.JwtProvider;
import com.quantcrux.security.UserPrincipal;
import com.quantcrux.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    UserService userService;
    
    @Autowired
    JwtProvider jwtProvider;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();
            
            String accessToken = jwtProvider.generateAccessToken(user);
            String refreshToken = jwtProvider.generateRefreshToken(user);
            
            // Update last login
            userService.updateLastLogin(user.getId());
            
            logger.info("User {} logged in successfully", user.getEmail());
            
            return ResponseEntity.ok(new JwtResponse(
                accessToken, 
                refreshToken, 
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
            ));
            
        } catch (Exception e) {
            logger.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Invalid email or password"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.createUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getRole()
            );
            
            logger.info("New user registered: {}", user.getEmail());
            
            return ResponseEntity.ok(new ApiResponse(true, "User registered successfully"));
            
        } catch (RuntimeException e) {
            logger.error("Registration failed", e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            if (jwtProvider.validateToken(refreshToken) && jwtProvider.isRefreshToken(refreshToken)) {
                String userId = jwtProvider.getUserIdFromToken(refreshToken).toString();
                User user = userService.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                String newAccessToken = jwtProvider.generateAccessToken(user);
                
                return ResponseEntity.ok(new ApiResponse(true, "Token refreshed successfully", 
                    new JwtResponse(newAccessToken, refreshToken, user.getId(), 
                                  user.getUsername(), user.getEmail(), user.getFullName(), user.getRole())));
            } else {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid refresh token"));
            }
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Token refresh failed"));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        
        return ResponseEntity.ok(new ApiResponse(true, "Profile retrieved successfully", 
            new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail(), 
                                  user.getFullName(), user.getRole(), user.getAccountStatus(),
                                  user.getLastLogin(), user.getCreatedAt())));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // In a real implementation, you might want to blacklist the token
        // For now, we'll just return success as the client will remove the token
        logger.info("User {} logged out", userPrincipal.getUser().getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }
    
    @GetMapping("/roles")
    public ResponseEntity<?> getUserRoles() {
        return ResponseEntity.ok(new ApiResponse(true, "Roles retrieved successfully", UserRole.values()));
    }
}