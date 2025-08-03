package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_history", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "date"}))
public class PortfolioHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate date;
    
    @NotNull
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal nav;
    
    @Column(name = "total_return_pct", precision = 10, scale = 6)
    private BigDecimal totalReturnPct;
    
    @Column(name = "daily_return_pct", precision = 10, scale = 6)
    private BigDecimal dailyReturnPct;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal contributions = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal withdrawals = BigDecimal.ZERO;
    
    // Performance metrics for the day
    @Column(precision = 8, scale = 6)
    private BigDecimal volatility;
    
    @Column(name = "var_95", precision = 15, scale = 2)
    private BigDecimal var95;
    
    @Column(precision = 8, scale = 6)
    private BigDecimal beta;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public PortfolioHistory() {}
    
    public PortfolioHistory(Portfolio portfolio, LocalDate date, BigDecimal nav) {
        this.portfolio = portfolio;
        this.date = date;
        this.nav = nav;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public BigDecimal getNav() { return nav; }
    public void setNav(BigDecimal nav) { this.nav = nav; }
    
    public BigDecimal getTotalReturnPct() { return totalReturnPct; }
    public void setTotalReturnPct(BigDecimal totalReturnPct) { this.totalReturnPct = totalReturnPct; }
    
    public BigDecimal getDailyReturnPct() { return dailyReturnPct; }
    public void setDailyReturnPct(BigDecimal dailyReturnPct) { this.dailyReturnPct = dailyReturnPct; }
    
    public BigDecimal getContributions() { return contributions; }
    public void setContributions(BigDecimal contributions) { this.contributions = contributions; }
    
    public BigDecimal getWithdrawals() { return withdrawals; }
    public void setWithdrawals(BigDecimal withdrawals) { this.withdrawals = withdrawals; }
    
    public BigDecimal getVolatility() { return volatility; }
    public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }
    
    public BigDecimal getVar95() { return var95; }
    public void setVar95(BigDecimal var95) { this.var95 = var95; }
    
    public BigDecimal getBeta() { return beta; }
    public void setBeta(BigDecimal beta) { this.beta = beta; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}