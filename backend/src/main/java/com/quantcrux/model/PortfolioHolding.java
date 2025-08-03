package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_holdings")
public class PortfolioHolding {
    
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
    
    @NotNull
    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal quantity;
    
    @NotNull
    @Column(name = "avg_price", precision = 15, scale = 6, nullable = false)
    private BigDecimal avgPrice;
    
    @Column(name = "latest_price", precision = 15, scale = 6)
    private BigDecimal latestPrice;
    
    @Column(name = "market_value", precision = 15, scale = 2)
    private BigDecimal marketValue;
    
    @Column(name = "cost_basis", precision = 15, scale = 2)
    private BigDecimal costBasis;
    
    @Column(name = "unrealized_pnl", precision = 15, scale = 2)
    private BigDecimal unrealizedPnl;
    
    @Column(name = "realized_pnl", precision = 15, scale = 2)
    private BigDecimal realizedPnl = BigDecimal.ZERO;
    
    @Column(length = 50)
    private String sector;
    
    @Column(name = "asset_class", length = 50)
    private String assetClass;
    
    @Column(name = "weight_pct", precision = 8, scale = 4)
    private BigDecimal weightPct;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public PortfolioHolding() {}
    
    public PortfolioHolding(Portfolio portfolio, InstrumentType instrumentType, String symbol, 
                           BigDecimal quantity, BigDecimal avgPrice) {
        this.portfolio = portfolio;
        this.instrumentType = instrumentType;
        this.symbol = symbol;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.costBasis = quantity.multiply(avgPrice);
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
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getAvgPrice() { return avgPrice; }
    public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }
    
    public BigDecimal getLatestPrice() { return latestPrice; }
    public void setLatestPrice(BigDecimal latestPrice) { this.latestPrice = latestPrice; }
    
    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }
    
    public BigDecimal getCostBasis() { return costBasis; }
    public void setCostBasis(BigDecimal costBasis) { this.costBasis = costBasis; }
    
    public BigDecimal getUnrealizedPnl() { return unrealizedPnl; }
    public void setUnrealizedPnl(BigDecimal unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }
    
    public BigDecimal getRealizedPnl() { return realizedPnl; }
    public void setRealizedPnl(BigDecimal realizedPnl) { this.realizedPnl = realizedPnl; }
    
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    
    public String getAssetClass() { return assetClass; }
    public void setAssetClass(String assetClass) { this.assetClass = assetClass; }
    
    public BigDecimal getWeightPct() { return weightPct; }
    public void setWeightPct(BigDecimal weightPct) { this.weightPct = weightPct; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}