package com.quantcrux.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "session_token", unique = true, nullable = false)
    private String sessionToken;
    
    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;
    
    @Column(name = "logout_time")
    private LocalDateTime logoutTime;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public UserSession() {}
    
    public UserSession(User user, String sessionToken, String ipAddress, String userAgent) {
        this.user = user;
        this.sessionToken = sessionToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.loginTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    
    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}