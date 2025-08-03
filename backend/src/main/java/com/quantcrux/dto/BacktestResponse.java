package com.quantcrux.dto;

import com.quantcrux.model.BacktestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BacktestResponse {
    
    private UUID id;
    private String name;
    private String strategyName;
    private UUID strategyId;
    private String symbol;
    private String timeframe;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal initialCapital;
    private BacktestStatus status;
    private Integer progress;
    private String errorMessage;
    
    // Results summary
    private BigDecimal finalCapital;
    private BigDecimal totalReturn;
    private Integer totalTrades;
    private Integer winningTrades;
    private Integer losingTrades;
    
    // Key metrics
    private BigDecimal sharpeRatio;
    private BigDecimal sortinoRatio;
    private BigDecimal maxDrawdown;
    private Integer maxDrawdownDuration;
    private BigDecimal cagr;
    private BigDecimal volatility;
    private BigDecimal profitFactor;
    private BigDecimal winRate;
    private Integer avgTradeDuration;
    
    // Charts data
    private List<EquityPoint> equityCurve;
    private List<DrawdownPoint> drawdownCurve;
    private Object monthlyReturns;
    
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    // Nested classes for chart data
    public static class EquityPoint {
        private LocalDateTime timestamp;
        private BigDecimal equity;
        
        public EquityPoint() {}
        
        public EquityPoint(LocalDateTime timestamp, BigDecimal equity) {
            this.timestamp = timestamp;
            this.equity = equity;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public BigDecimal getEquity() { return equity; }
        public void setEquity(BigDecimal equity) { this.equity = equity; }
    }
    
    public static class DrawdownPoint {
        private LocalDateTime timestamp;
        private BigDecimal drawdown;
        
        public DrawdownPoint() {}
        
        public DrawdownPoint(LocalDateTime timestamp, BigDecimal drawdown) {
            this.timestamp = timestamp;
            this.drawdown = drawdown;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public BigDecimal getDrawdown() { return drawdown; }
        public void setDrawdown(BigDecimal drawdown) { this.drawdown = drawdown; }
    }
    
    // Constructors
    public BacktestResponse() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getStrategyName() { return strategyName; }
    public void setStrategyName(String strategyName) { this.strategyName = strategyName; }
    
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    
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
    
    public BacktestStatus getStatus() { return status; }
    public void setStatus(BacktestStatus status) { this.status = status; }
    
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public BigDecimal getFinalCapital() { return finalCapital; }
    public void setFinalCapital(BigDecimal finalCapital) { this.finalCapital = finalCapital; }
    
    public BigDecimal getTotalReturn() { return totalReturn; }
    public void setTotalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; }
    
    public Integer getTotalTrades() { return totalTrades; }
    public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }
    
    public Integer getWinningTrades() { return winningTrades; }
    public void setWinningTrades(Integer winningTrades) { this.winningTrades = winningTrades; }
    
    public Integer getLosingTrades() { return losingTrades; }
    public void setLosingTrades(Integer losingTrades) { this.losingTrades = losingTrades; }
    
    public BigDecimal getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
    
    public BigDecimal getSortinoRatio() { return sortinoRatio; }
    public void setSortinoRatio(BigDecimal sortinoRatio) { this.sortinoRatio = sortinoRatio; }
    
    public BigDecimal getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
    
    public Integer getMaxDrawdownDuration() { return maxDrawdownDuration; }
    public void setMaxDrawdownDuration(Integer maxDrawdownDuration) { this.maxDrawdownDuration = maxDrawdownDuration; }
    
    public BigDecimal getCagr() { return cagr; }
    public void setCagr(BigDecimal cagr) { this.cagr = cagr; }
    
    public BigDecimal getVolatility() { return volatility; }
    public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }
    
    public BigDecimal getProfitFactor() { return profitFactor; }
    public void setProfitFactor(BigDecimal profitFactor) { this.profitFactor = profitFactor; }
    
    public BigDecimal getWinRate() { return winRate; }
    public void setWinRate(BigDecimal winRate) { this.winRate = winRate; }
    
    public Integer getAvgTradeDuration() { return avgTradeDuration; }
    public void setAvgTradeDuration(Integer avgTradeDuration) { this.avgTradeDuration = avgTradeDuration; }
    
    public List<EquityPoint> getEquityCurve() { return equityCurve; }
    public void setEquityCurve(List<EquityPoint> equityCurve) { this.equityCurve = equityCurve; }
    
    public List<DrawdownPoint> getDrawdownCurve() { return drawdownCurve; }
    public void setDrawdownCurve(List<DrawdownPoint> drawdownCurve) { this.drawdownCurve = drawdownCurve; }
    
    public Object getMonthlyReturns() { return monthlyReturns; }
    public void setMonthlyReturns(Object monthlyReturns) { this.monthlyReturns = monthlyReturns; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}