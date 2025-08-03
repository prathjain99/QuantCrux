package com.quantcrux.dto;

import com.quantcrux.model.SignalType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SignalEvaluationResponse {
    
    private SignalType signal;
    private BigDecimal currentPrice;
    private Map<String, Object> indicatorValues;
    private List<String> matchedRules;
    private BigDecimal confidenceScore;
    private LocalDateTime evaluatedAt;
    private String message;
    
    // Constructors
    public SignalEvaluationResponse() {}
    
    public SignalEvaluationResponse(SignalType signal, BigDecimal currentPrice) {
        this.signal = signal;
        this.currentPrice = currentPrice;
        this.evaluatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public SignalType getSignal() { return signal; }
    public void setSignal(SignalType signal) { this.signal = signal; }
    
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    
    public Map<String, Object> getIndicatorValues() { return indicatorValues; }
    public void setIndicatorValues(Map<String, Object> indicatorValues) { this.indicatorValues = indicatorValues; }
    
    public List<String> getMatchedRules() { return matchedRules; }
    public void setMatchedRules(List<String> matchedRules) { this.matchedRules = matchedRules; }
    
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}