package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "positions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "symbol", "instrument_type"}))
public class Position {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @Column(name = "instrument_id")
    private UUID instrumentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false)
    private InstrumentType instrumentType;
    
    @NotNull
    @Column(nullable = false, length = 50)
    private String symbol;
    
    // Position details
    @NotNull
    @Column(name = "net_quantity", precision = 20, scale = 8, nullable = false)
    private BigDecimal netQuantity = BigDecimal.ZERO;
    
    @Column(name = "avg_price", precision = 15, scale = 6)
    private BigDecimal avgPrice;
    
    @Column(name = "cost_basis", precision = 15, scale = 2)
    private BigDecimal costBasis;
    
    @Column(name = "market_value", precision = 15, scale = 2)
    private BigDecimal marketValue;
    
    @Column(name = "unrealized_pnl", precision = 15, scale = 2)
    private BigDecimal unrealizedPnl;
    
    @Column(name = "realized_pnl", precision = 15, scale = 2)
    private BigDecimal realizedPnl = BigDecimal.ZERO;
    
    // Risk metrics
    @Column(precision = 10, scale = 6)
    private BigDecimal delta;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal gamma;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal theta;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal vega;
    
    // Metadata
    @Column(name = "first_trade_date")
    private LocalDate firstTradeDate;
    
    @Column(name = "last_trade_date")
    private LocalDate lastTradeDate;
    
    @Column(name = "total_trades")
    private Integer totalTrades = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Position() {}
    
    public Position(Portfolio portfolio, InstrumentType instrumentType, String symbol) {
        this.portfolio = portfolio;
        this.instrumentType = instrumentType;
        this.symbol = symbol;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    
    public UUID getInstrumentId() { return instrumentId; }
    public void setInstrumentId(UUID instrumentId) { this.instrumentId = instrumentId; }
    
    public InstrumentType getInstrumentType() { return instrumentType; }
    public void setInstrumentType(InstrumentType instrumentType) { this.instrumentType = instrumentType; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public BigDecimal getNetQuantity() { return netQuantity; }
    public void setNetQuantity(BigDecimal netQuantity) { this.netQuantity = netQuantity; }
    
    public BigDecimal getAvgPrice() { return avgPrice; }
    public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }
    
    public BigDecimal getCostBasis() { return costBasis; }
    public void setCostBasis(BigDecimal costBasis) { this.costBasis = costBasis; }
    
    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }
    
    public BigDecimal getUnrealizedPnl() { return unrealizedPnl; }
    public void setUnrealizedPnl(BigDecimal unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }
    
    public BigDecimal getRealizedPnl() { return realizedPnl; }
    public void setRealizedPnl(BigDecimal realizedPnl) { this.realizedPnl = realizedPnl; }
    
    public BigDecimal getDelta() { return delta; }
    public void setDelta(BigDecimal delta) { this.delta = delta; }
    
    public BigDecimal getGamma() { return gamma; }
    public void setGamma(BigDecimal gamma) { this.gamma = gamma; }
    
    public BigDecimal getTheta() { return theta; }
    public void setTheta(BigDecimal theta) { this.theta = theta; }
    
    public BigDecimal getVega() { return vega; }
    public void setVega(BigDecimal vega) { this.vega = vega; }
    
    public LocalDate getFirstTradeDate() { return firstTradeDate; }
    public void setFirstTradeDate(LocalDate firstTradeDate) { this.firstTradeDate = firstTradeDate; }
    
    public LocalDate getLastTradeDate() { return lastTradeDate; }
    public void setLastTradeDate(LocalDate lastTradeDate) { this.lastTradeDate = lastTradeDate; }
    
    public Integer getTotalTrades() { return totalTrades; }
    public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}