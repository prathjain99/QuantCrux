package com.quantcrux.dto;

import com.quantcrux.model.StrategyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class StrategyRequest {
    
    @NotBlank
    private String name;
    
    private String description;
    
    @NotBlank
    private String symbol;
    
    private String timeframe = "1m";
    
    @NotNull
    private String configJson;
    
    private StrategyStatus status = StrategyStatus.DRAFT;
    
    private List<String> tags;
    
    // Constructors
    public StrategyRequest() {}
    
    public StrategyRequest(String name, String symbol, String configJson) {
        this.name = name;
        this.symbol = symbol;
        this.configJson = configJson;
    }
    
    // Getters and Setters
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
}