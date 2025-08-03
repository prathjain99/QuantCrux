package com.quantcrux.dto;

import com.quantcrux.model.BacktestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class BacktestRequest {
    
    @NotNull
    private UUID strategyId;
    
    private UUID strategyVersionId;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String symbol;
    
    private String timeframe = "1d";
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    private BigDecimal initialCapital = BigDecimal.valueOf(100000.00);
    
    private BigDecimal commissionRate = BigDecimal.valueOf(0.001);
    
    private BigDecimal slippageRate = BigDecimal.valueOf(0.0005);
    
    // Advanced options
    private Boolean monteCarloEnabled = false;
    
    private Integer monteCarloRuns = 1000;
    
    private Boolean walkForwardEnabled = false;
    
    private String benchmarkSymbol;
    
    // Constructors
    public BacktestRequest() {}
    
    public BacktestRequest(UUID strategyId, String name, String symbol, LocalDate startDate, LocalDate endDate) {
        this.strategyId = strategyId;
        this.name = name;
        this.symbol = symbol;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    
    public UUID getStrategyVersionId() { return strategyVersionId; }
    public void setStrategyVersionId(UUID strategyVersionId) { this.strategyVersionId = strategyVersionId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public BigDecimal getInitialCapital() { return initialCapital; }
    public void setInitialCapital(BigDecimal initialCapital) { this.initialCapital = initialCapital; }
    
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    
    public BigDecimal getSlippageRate() { return slippageRate; }
    public void setSlippageRate(BigDecimal slippageRate) { this.slippageRate = slippageRate; }
    
    public Boolean getMonteCarloEnabled() { return monteCarloEnabled; }
    public void setMonteCarloEnabled(Boolean monteCarloEnabled) { this.monteCarloEnabled = monteCarloEnabled; }
    
    public Integer getMonteCarloRuns() { return monteCarloRuns; }
    public void setMonteCarloRuns(Integer monteCarloRuns) { this.monteCarloRuns = monteCarloRuns; }
    
    public Boolean getWalkForwardEnabled() { return walkForwardEnabled; }
    public void setWalkForwardEnabled(Boolean walkForwardEnabled) { this.walkForwardEnabled = walkForwardEnabled; }
    
    public String getBenchmarkSymbol() { return benchmarkSymbol; }
    public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
}