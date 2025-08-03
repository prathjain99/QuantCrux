package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "data_sources")
public class DataSource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotBlank
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    @NotBlank
    @Column(name = "api_url", nullable = false)
    private String apiUrl;
    
    @Column(name = "api_key_hash")
    private String apiKeyHash;
    
    // Configuration
    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute = 60;
    
    @Column(name = "rate_limit_per_day")
    private Integer rateLimitPerDay = 1000;
    
    @Column
    private Integer priority = 1;
    
    // Status tracking
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_request_at")
    private LocalDateTime lastRequestAt;
    
    @Column(name = "requests_today")
    private Integer requestsToday = 0;
    
    @Column(name = "requests_this_minute")
    private Integer requestsThisMinute = 0;
    
    // Error tracking
    @Column(name = "consecutive_failures")
    private Integer consecutiveFailures = 0;
    
    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;
    
    @Column(name = "last_error_at")
    private LocalDateTime lastErrorAt;
    
    // Supported features
    @Column(name = "supports_live_prices")
    private Boolean supportsLivePrices = true;
    
    @Column(name = "supports_historical")
    private Boolean supportsHistorical = true;
    
    @Column(name = "supports_crypto")
    private Boolean supportsCrypto = false;
    
    @Column(name = "supports_forex")
    private Boolean supportsForex = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public DataSource() {}
    
    public DataSource(String name, String apiUrl) {
        this.name = name;
        this.apiUrl = apiUrl;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    
    public String getApiKeyHash() { return apiKeyHash; }
    public void setApiKeyHash(String apiKeyHash) { this.apiKeyHash = apiKeyHash; }
    
    public Integer getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(Integer rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    
    public Integer getRateLimitPerDay() { return rateLimitPerDay; }
    public void setRateLimitPerDay(Integer rateLimitPerDay) { this.rateLimitPerDay = rateLimitPerDay; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getLastRequestAt() { return lastRequestAt; }
    public void setLastRequestAt(LocalDateTime lastRequestAt) { this.lastRequestAt = lastRequestAt; }
    
    public Integer getRequestsToday() { return requestsToday; }
    public void setRequestsToday(Integer requestsToday) { this.requestsToday = requestsToday; }
    
    public Integer getRequestsThisMinute() { return requestsThisMinute; }
    public void setRequestsThisMinute(Integer requestsThisMinute) { this.requestsThisMinute = requestsThisMinute; }
    
    public Integer getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(Integer consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
    
    public String getLastErrorMessage() { return lastErrorMessage; }
    public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
    
    public LocalDateTime getLastErrorAt() { return lastErrorAt; }
    public void setLastErrorAt(LocalDateTime lastErrorAt) { this.lastErrorAt = lastErrorAt; }
    
    public Boolean getSupportsLivePrices() { return supportsLivePrices; }
    public void setSupportsLivePrices(Boolean supportsLivePrices) { this.supportsLivePrices = supportsLivePrices; }
    
    public Boolean getSupportsHistorical() { return supportsHistorical; }
    public void setSupportsHistorical(Boolean supportsHistorical) { this.supportsHistorical = supportsHistorical; }
    
    public Boolean getSupportsCrypto() { return supportsCrypto; }
    public void setSupportsCrypto(Boolean supportsCrypto) { this.supportsCrypto = supportsCrypto; }
    
    public Boolean getSupportsForex() { return supportsForex; }
    public void setSupportsForex(Boolean supportsForex) { this.supportsForex = supportsForex; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}