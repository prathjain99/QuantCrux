package com.quantcrux.dto;

import com.quantcrux.model.InstrumentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PositionResponse {
    
    private UUID id;
    private UUID portfolioId;
    private String portfolioName;
    private UUID instrumentId;
    private InstrumentType instrumentType;
    private String symbol;
    
    // Position details
    private BigDecimal netQuantity;
    private BigDecimal avgPrice;
    private BigDecimal costBasis;
    private BigDecimal marketValue;
    private BigDecimal unrealizedPnl;
    private BigDecimal realizedPnl;
    
    // Current market data
    private BigDecimal currentPrice;
    private BigDecimal dayChange;
    private BigDecimal dayChangePercent;
    
    // Risk metrics
    private BigDecimal delta;
    private BigDecimal gamma;
    private BigDecimal theta;
    private BigDecimal vega;
    
    // Metadata
    private LocalDate firstTradeDate;
    private LocalDate lastTradeDate;
    private Integer totalTrades;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Calculated fields
    private BigDecimal weightPercent; // % of portfolio
    private String positionType; // "Long", "Short", "Flat"
    private BigDecimal returnPercent; // Position return %
    
    // Constructors
    public PositionResponse() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }
    
    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }
    
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
    
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    
    public BigDecimal getDayChange() { return dayChange; }
    public void setDayChange(BigDecimal dayChange) { this.dayChange = dayChange; }
    
    public BigDecimal getDayChangePercent() { return dayChangePercent; }
    public void setDayChangePercent(BigDecimal dayChangePercent) { this.dayChangePercent = dayChangePercent; }
    
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
    
    public BigDecimal getWeightPercent() { return weightPercent; }
    public void setWeightPercent(BigDecimal weightPercent) { this.weightPercent = weightPercent; }
    
    public String getPositionType() { return positionType; }
    public void setPositionType(String positionType) { this.positionType = positionType; }
    
    public BigDecimal getReturnPercent() { return returnPercent; }
    public void setReturnPercent(BigDecimal returnPercent) { this.returnPercent = returnPercent; }
}