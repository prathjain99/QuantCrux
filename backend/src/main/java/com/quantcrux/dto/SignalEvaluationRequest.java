package com.quantcrux.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SignalEvaluationRequest {
    
    @NotBlank
    private String symbol;
    
    @NotNull
    private String configJson;
    
    private String timeframe = "1m";
    
    // Constructors
    public SignalEvaluationRequest() {}
    
    public SignalEvaluationRequest(String symbol, String configJson) {
        this.symbol = symbol;
        this.configJson = configJson;
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
}