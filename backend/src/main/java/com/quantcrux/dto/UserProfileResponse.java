package com.quantcrux.dto;

import com.quantcrux.model.AccountStatus;
import com.quantcrux.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfileResponse {
    
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private AccountStatus accountStatus;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    
    public UserProfileResponse(UUID id, String username, String email, String fullName, 
                              UserRole role, AccountStatus accountStatus, 
                              LocalDateTime lastLogin, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.accountStatus = accountStatus;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}