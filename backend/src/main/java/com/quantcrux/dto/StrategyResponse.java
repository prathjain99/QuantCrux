package com.quantcrux.dto;

import com.quantcrux.model.StrategyStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class StrategyResponse {
    
    private UUID id;
    private String name;
    private String description;
    private String symbol;
    private String timeframe;
    private String configJson;
    private StrategyStatus status;
    private List<String> tags;
    private Integer currentVersion;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public StrategyResponse() {}
    
    public StrategyResponse(UUID id, String name, String symbol, String configJson, StrategyStatus status) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.configJson = configJson;
        this.status = status;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    
    public StrategyStatus getStatus() { return status; }
    public void setStatus(StrategyStatus status) { this.status = status; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public Integer getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(Integer currentVersion) { this.currentVersion = currentVersion; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}