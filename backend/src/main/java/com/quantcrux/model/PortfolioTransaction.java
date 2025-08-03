package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_transactions")
public class PortfolioTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(length = 50)
    private String symbol;
    
    @Column(precision = 20, scale = 8)
    private BigDecimal quantity;
    
    @Column(precision = 15, scale = 6)
    private BigDecimal price;
    
    @NotNull
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // References to other modules
    @Column(name = "strategy_id")
    private UUID strategyId;
    
    @Column(name = "product_id")
    private UUID productId;
    
    @Column(name = "backtest_id")
    private UUID backtestId;
    
    @Column(name = "executed_at")
    private LocalDateTime executedAt = LocalDateTime.now();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public PortfolioTransaction() {}
    
    public PortfolioTransaction(Portfolio portfolio, TransactionType transactionType, BigDecimal amount) {
        this.portfolio = portfolio;
        this.transactionType = transactionType;
        this.amount = amount;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    
    public UUID getBacktestId() { return backtestId; }
    public void setBacktestId(UUID backtestId) { this.backtestId = backtestId; }
    
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}