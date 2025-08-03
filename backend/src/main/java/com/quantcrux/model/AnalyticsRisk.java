package com.quantcrux.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analytics_risk")
public class AnalyticsRisk {
    
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
    
    // Risk metrics
    @Column(name = "var_95", precision = 15, scale = 2)
    private BigDecimal var95;
    
    @Column(name = "var_99", precision = 15, scale = 2)
    private BigDecimal var99;
    
    @Column(precision = 8, scale = 6)
    private BigDecimal volatility;
    
    @Column(precision = 8, scale = 6)
    private BigDecimal beta;
    
    @Column(precision = 8, scale = 6)
    private BigDecimal alpha;
    
    @Column(name = "sharpe_ratio", precision = 8, scale = 6)
    private BigDecimal sharpeRatio;
    
    @Column(name = "sortino_ratio", precision = 8, scale = 6)
    private BigDecimal sortinoRatio;
    
    @Column(name = "max_drawdown", precision = 8, scale = 6)
    private BigDecimal maxDrawdown;
    
    @Column(name = "max_drawdown_duration")
    private Integer maxDrawdownDuration;
    
    // Additional metrics
    @Column(precision = 8, scale = 6)
    private BigDecimal skewness;
    
    @Column(precision = 8, scale = 6)
    private BigDecimal kurtosis;
    
    @Column(name = "calmar_ratio", precision = 8, scale = 6)
    private BigDecimal calmarRatio;
    
    @Column(name = "information_ratio", precision = 8, scale = 6)
    private BigDecimal informationRatio;
    
    // Benchmark data
    @Column(name = "benchmark_symbol", length = 20)
    private String benchmarkSymbol = "SPY";
    
    @Column(name = "correlation_to_benchmark", precision = 8, scale = 6)
    private BigDecimal correlationToBenchmark;
    
    @Column(name = "tracking_error", precision = 8, scale = 6)
    private BigDecimal trackingError;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public AnalyticsRisk() {}
    
    public AnalyticsRisk(Portfolio portfolio, LocalDate calculationDate) {
        this.portfolio = portfolio;
        this.calculationDate = calculationDate;
    }
    
    public AnalyticsRisk(Strategy strategy, LocalDate calculationDate) {
        this.strategy = strategy;
        this.calculationDate = calculationDate;
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
    
    public BigDecimal getVar95() { return var95; }
    public void setVar95(BigDecimal var95) { this.var95 = var95; }
    
    public BigDecimal getVar99() { return var99; }
    public void setVar99(BigDecimal var99) { this.var99 = var99; }
    
    public BigDecimal getVolatility() { return volatility; }
    public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }
    
    public BigDecimal getBeta() { return beta; }
    public void setBeta(BigDecimal beta) { this.beta = beta; }
    
    public BigDecimal getAlpha() { return alpha; }
    public void setAlpha(BigDecimal alpha) { this.alpha = alpha; }
    
    public BigDecimal getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
    
    public BigDecimal getSortinoRatio() { return sortinoRatio; }
    public void setSortinoRatio(BigDecimal sortinoRatio) { this.sortinoRatio = sortinoRatio; }
    
    public BigDecimal getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
    
    public Integer getMaxDrawdownDuration() { return maxDrawdownDuration; }
    public void setMaxDrawdownDuration(Integer maxDrawdownDuration) { this.maxDrawdownDuration = maxDrawdownDuration; }
    
    public BigDecimal getSkewness() { return skewness; }
    public void setSkewness(BigDecimal skewness) { this.skewness = skewness; }
    
    public BigDecimal getKurtosis() { return kurtosis; }
    public void setKurtosis(BigDecimal kurtosis) { this.kurtosis = kurtosis; }
    
    public BigDecimal getCalmarRatio() { return calmarRatio; }
    public void setCalmarRatio(BigDecimal calmarRatio) { this.calmarRatio = calmarRatio; }
    
    public BigDecimal getInformationRatio() { return informationRatio; }
    public void setInformationRatio(BigDecimal informationRatio) { this.informationRatio = informationRatio; }
    
    public String getBenchmarkSymbol() { return benchmarkSymbol; }
    public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
    
    public BigDecimal getCorrelationToBenchmark() { return correlationToBenchmark; }
    public void setCorrelationToBenchmark(BigDecimal correlationToBenchmark) { this.correlationToBenchmark = correlationToBenchmark; }
    
    public BigDecimal getTrackingError() { return trackingError; }
    public void setTrackingError(BigDecimal trackingError) { this.trackingError = trackingError; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}