package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "portfolios")
public class Portfolio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull
    @Column(name = "initial_capital", precision = 15, scale = 2, nullable = false)
    private BigDecimal initialCapital = BigDecimal.valueOf(100000.00);
    
    @NotNull
    @Column(name = "current_nav", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentNav = BigDecimal.valueOf(100000.00);
    
    @NotNull
    @Column(name = "cash_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal cashBalance = BigDecimal.valueOf(100000.00);
    
    @Column(name = "total_pnl", precision = 15, scale = 2)
    private BigDecimal totalPnl = BigDecimal.ZERO;
    
    @Column(name = "total_return_pct", precision = 10, scale = 6)
    private BigDecimal totalReturnPct = BigDecimal.ZERO;
    
    // Risk metrics
    @Column(name = "var_95", precision = 15, scale = 2)
    private BigDecimal var95;
    
    @Column(precision = 8, scale = 6)
    private BigDecimal volatility;
    
    @Column(precision = 8, scale = 6)
    private BigDecimal beta;
    
    @Column(name = "max_drawdown", precision = 8, scale = 6)
    private BigDecimal maxDrawdown;
    
    @Column(name = "sharpe_ratio", precision = 8, scale = 6)
    private BigDecimal sharpeRatio;
    
    // Status and settings
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PortfolioStatus status = PortfolioStatus.ACTIVE;
    
    @Column(length = 10)
    private String currency = "USD";
    
    @Column(name = "benchmark_symbol", length = 20)
    private String benchmarkSymbol = "SPY";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PortfolioHolding> holdings;
    
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PortfolioHistory> history;
    
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PortfolioTransaction> transactions;
    
    // Constructors
    public Portfolio() {}
    
    public Portfolio(User owner, String name, BigDecimal initialCapital) {
        this.owner = owner;
        this.name = name;
        this.initialCapital = initialCapital;
        this.currentNav = initialCapital;
        this.cashBalance = initialCapital;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    
    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getInitialCapital() { return initialCapital; }
    public void setInitialCapital(BigDecimal initialCapital) { this.initialCapital = initialCapital; }
    
    public BigDecimal getCurrentNav() { return currentNav; }
    public void setCurrentNav(BigDecimal currentNav) { this.currentNav = currentNav; }
    
    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }
    
    public BigDecimal getTotalPnl() { return totalPnl; }
    public void setTotalPnl(BigDecimal totalPnl) { this.totalPnl = totalPnl; }
    
    public BigDecimal getTotalReturnPct() { return totalReturnPct; }
    public void setTotalReturnPct(BigDecimal totalReturnPct) { this.totalReturnPct = totalReturnPct; }
    
    public BigDecimal getVar95() { return var95; }
    public void setVar95(BigDecimal var95) { this.var95 = var95; }
    
    public BigDecimal getVolatility() { return volatility; }
    public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }
    
    public BigDecimal getBeta() { return beta; }
    public void setBeta(BigDecimal beta) { this.beta = beta; }
    
    public BigDecimal getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
    
    public BigDecimal getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
    
    public PortfolioStatus getStatus() { return status; }
    public void setStatus(PortfolioStatus status) { this.status = status; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getBenchmarkSymbol() { return benchmarkSymbol; }
    public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<PortfolioHolding> getHoldings() { return holdings; }
    public void setHoldings(List<PortfolioHolding> holdings) { this.holdings = holdings; }
    
    public List<PortfolioHistory> getHistory() { return history; }
    public void setHistory(List<PortfolioHistory> history) { this.history = history; }
    
    public List<PortfolioTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<PortfolioTransaction> transactions) { this.transactions = transactions; }
}