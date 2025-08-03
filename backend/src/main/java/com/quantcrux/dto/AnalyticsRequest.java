package com.quantcrux.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public class AnalyticsRequest {
    
    private UUID portfolioId;
    private UUID strategyId;
    
    @NotNull
    private LocalDate periodStart;
    
    @NotNull
    private LocalDate periodEnd;
    
    private String benchmarkSymbol = "SPY";
    private Boolean includeCorrelations = false;
    private Boolean includeAttribution = false;
    
    // Constructors
    public AnalyticsRequest() {}
    
    public AnalyticsRequest(UUID portfolioId, LocalDate periodStart, LocalDate periodEnd) {
        this.portfolioId = portfolioId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }
    
    // Getters and Setters
    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }
    
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    
    public String getBenchmarkSymbol() { return benchmarkSymbol; }
    public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
    
    public Boolean getIncludeCorrelations() { return includeCorrelations; }
    public void setIncludeCorrelations(Boolean includeCorrelations) { this.includeCorrelations = includeCorrelations; }
    
    public Boolean getIncludeAttribution() { return includeAttribution; }
    public void setIncludeAttribution(Boolean includeAttribution) { this.includeAttribution = includeAttribution; }
}