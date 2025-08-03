package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "backtests")
public class Backtest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", nullable = false)
    private Strategy strategy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_version_id")
    private StrategyVersion strategyVersion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @NotBlank
    @Column(nullable = false, length = 20)
    private String symbol;
    
    @Column(nullable = false, length = 10)
    private String timeframe;
    
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "initial_capital", precision = 15, scale = 2)
    private BigDecimal initialCapital = BigDecimal.valueOf(100000.00);
    
    @Column(name = "commission_rate", precision = 8, scale = 6)
    private BigDecimal commissionRate = BigDecimal.valueOf(0.001);
    
    @Column(name = "slippage_rate", precision = 8, scale = 6)
    private BigDecimal slippageRate = BigDecimal.valueOf(0.0005);
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BacktestStatus status = BacktestStatus.PENDING;
    
    @Column
    private Integer progress = 0;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    // Results summary
    @Column(name = "final_capital", precision = 15, scale = 2)
    private BigDecimal finalCapital;
    
    @Column(name = "total_return", precision = 10, scale = 6)
    private BigDecimal totalReturn;
    
    @Column(name = "total_trades")
    private Integer totalTrades = 0;
    
    @Column(name = "winning_trades")
    private Integer winningTrades = 0;
    
    @Column(name = "losing_trades")
    private Integer losingTrades = 0;
    
    // Key metrics
    @Column(name = "sharpe_ratio", precision = 10, scale = 6)
    private BigDecimal sharpeRatio;
    
    @Column(name = "sortino_ratio", precision = 10, scale = 6)
    private BigDecimal sortinoRatio;
    
    @Column(name = "max_drawdown", precision = 10, scale = 6)
    private BigDecimal maxDrawdown;
    
    @Column(name = "max_drawdown_duration")
    private Integer maxDrawdownDuration;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal cagr;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal volatility;
    
    @Column(name = "profit_factor", precision = 10, scale = 6)
    private BigDecimal profitFactor;
    
    @Column(name = "win_rate", precision = 8, scale = 6)
    private BigDecimal winRate;
    
    @Column(name = "avg_trade_duration")
    private Integer avgTradeDuration;
    
    // Detailed results (JSON)
    @Column(name = "equity_curve", columnDefinition = "TEXT")
    private String equityCurve;
    
    @Column(name = "drawdown_curve", columnDefinition = "TEXT")
    private String drawdownCurve;
    
    @Column(name = "monthly_returns", columnDefinition = "TEXT")
    private String monthlyReturns;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @OneToMany(mappedBy = "backtest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BacktestTrade> trades;
    
    // Constructors
    public Backtest() {}
    
    public Backtest(Strategy strategy, User user, String name, String symbol, String timeframe, 
                   LocalDate startDate, LocalDate endDate) {
        this.strategy = strategy;
        this.user = user;
        this.name = name;
        this.symbol = symbol;
        this.timeframe = timeframe;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Strategy getStrategy() { return strategy; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }
    
    public StrategyVersion getStrategyVersion() { return strategyVersion; }
    public void setStrategyVersion(StrategyVersion strategyVersion) { this.strategyVersion = strategyVersion; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
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
    
    public String getEquityCurve() { return equityCurve; }
    public void setEquityCurve(String equityCurve) { this.equityCurve = equityCurve; }
    
    public String getDrawdownCurve() { return drawdownCurve; }
    public void setDrawdownCurve(String drawdownCurve) { this.drawdownCurve = drawdownCurve; }
    
    public String getMonthlyReturns() { return monthlyReturns; }
    public void setMonthlyReturns(String monthlyReturns) { this.monthlyReturns = monthlyReturns; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public List<BacktestTrade> getTrades() { return trades; }
    public void setTrades(List<BacktestTrade> trades) { this.trades = trades; }
}