package com.quantcrux.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analytics_performance")
public class AnalyticsPerformance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id")
    private Strategy strategy;
    
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate = LocalDate.now();
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    // Return metrics
    @Column(name = "total_return", precision = 10, scale = 6)
    private BigDecimal totalReturn;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal cagr;
    
    @Column(name = "annualized_return", precision = 10, scale = 6)
    private BigDecimal annualizedReturn;
    
    @Column(name = "excess_return", precision = 10, scale = 6)
    private BigDecimal excessReturn;
    
    // Trade metrics
    @Column(name = "total_trades")
    private Integer totalTrades = 0;
    
    @Column(name = "winning_trades")
    private Integer winningTrades = 0;
    
    @Column(name = "losing_trades")
    private Integer losingTrades = 0;
    
    @Column(name = "win_rate", precision = 8, scale = 6)
    private BigDecimal winRate;
    
    @Column(name = "avg_win", precision = 15, scale = 2)
    private BigDecimal avgWin;
    
    @Column(name = "avg_loss", precision = 15, scale = 2)
    private BigDecimal avgLoss;
    
    @Column(name = "profit_factor", precision = 8, scale = 6)
    private BigDecimal profitFactor;
    
    // Efficiency metrics
    @Column(name = "trade_frequency", precision = 8, scale = 2)
    private BigDecimal tradeFrequency;
    
    @Column(name = "avg_holding_period")
    private Integer avgHoldingPeriod;
    
    @Column(name = "turnover_ratio", precision = 8, scale = 6)
    private BigDecimal turnoverRatio;
    
    // Benchmark comparison
    @Column(name = "benchmark_return", precision = 10, scale = 6)
    private BigDecimal benchmarkReturn;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal outperformance;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public AnalyticsPerformance() {}
    
    public AnalyticsPerformance(Portfolio portfolio, LocalDate periodStart, LocalDate periodEnd) {
        this.portfolio = portfolio;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }
    
    public AnalyticsPerformance(Strategy strategy, LocalDate periodStart, LocalDate periodEnd) {
        this.strategy = strategy;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    
    public Strategy getStrategy() { return strategy; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }
    
    public LocalDate getCalculationDate() { return calculationDate; }
    public void setCalculationDate(LocalDate calculationDate) { this.calculationDate = calculationDate; }
    
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    
    public BigDecimal getTotalReturn() { return totalReturn; }
    public void setTotalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; }
    
    public BigDecimal getCagr() { return cagr; }
    public void setCagr(BigDecimal cagr) { this.cagr = cagr; }
    
    public BigDecimal getAnnualizedReturn() { return annualizedReturn; }
    public void setAnnualizedReturn(BigDecimal annualizedReturn) { this.annualizedReturn = annualizedReturn; }
    
    public BigDecimal getExcessReturn() { return excessReturn; }
    public void setExcessReturn(BigDecimal excessReturn) { this.excessReturn = excessReturn; }
    
    public Integer getTotalTrades() { return totalTrades; }
    public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }
    
    public Integer getWinningTrades() { return winningTrades; }
    public void setWinningTrades(Integer winningTrades) { this.winningTrades = winningTrades; }
    
    public Integer getLosingTrades() { return losingTrades; }
    public void setLosingTrades(Integer losingTrades) { this.losingTrades = losingTrades; }
    
    public BigDecimal getWinRate() { return winRate; }
    public void setWinRate(BigDecimal winRate) { this.winRate = winRate; }
    
    public BigDecimal getAvgWin() { return avgWin; }
    public void setAvgWin(BigDecimal avgWin) { this.avgWin = avgWin; }
    
    public BigDecimal getAvgLoss() { return avgLoss; }
    public void setAvgLoss(BigDecimal avgLoss) { this.avgLoss = avgLoss; }
    
    public BigDecimal getProfitFactor() { return profitFactor; }
    public void setProfitFactor(BigDecimal profitFactor) { this.profitFactor = profitFactor; }
    
    public BigDecimal getTradeFrequency() { return tradeFrequency; }
    public void setTradeFrequency(BigDecimal tradeFrequency) { this.tradeFrequency = tradeFrequency; }
    
    public Integer getAvgHoldingPeriod() { return avgHoldingPeriod; }
    public void setAvgHoldingPeriod(Integer avgHoldingPeriod) { this.avgHoldingPeriod = avgHoldingPeriod; }
    
    public BigDecimal getTurnoverRatio() { return turnoverRatio; }
    public void setTurnoverRatio(BigDecimal turnoverRatio) { this.turnoverRatio = turnoverRatio; }
    
    public BigDecimal getBenchmarkReturn() { return benchmarkReturn; }
    public void setBenchmarkReturn(BigDecimal benchmarkReturn) { this.benchmarkReturn = benchmarkReturn; }
    
    public BigDecimal getOutperformance() { return outperformance; }
    public void setOutperformance(BigDecimal outperformance) { this.outperformance = outperformance; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}