package com.quantcrux.dto;

import com.quantcrux.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TradeResponse {
    
    private UUID id;
    private UUID orderId;
    private UUID portfolioId;
    private String portfolioName;
    private UUID instrumentId;
    private InstrumentType instrumentType;
    private String symbol;
    
    // Trade details
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private BigDecimal fees;
    
    // Execution quality
    private BigDecimal expectedPrice;
    private BigDecimal slippage;
    private String executionVenue;
    
    // Status and dates
    private TradeStatus status;
    private LocalDate tradeDate;
    private LocalDate settlementDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
    private LocalDateTime settledAt;
    
    // References
    private UUID strategyId;
    private String strategyName;
    private UUID productId;
    private String productName;
    
    // Metadata
    private String notes;
    private String executionId;
    private String userName;
    
    // Calculated fields
    private BigDecimal netAmount; // Total amount including fees
    private String executionQuality; // "Good", "Fair", "Poor" based on slippage
    
    // Constructors
    public TradeResponse() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
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
    
    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    
    public BigDecimal getExpectedPrice() { return expectedPrice; }
    public void setExpectedPrice(BigDecimal expectedPrice) { this.expectedPrice = expectedPrice; }
    
    public BigDecimal getSlippage() { return slippage; }
    public void setSlippage(BigDecimal slippage) { this.slippage = slippage; }
    
    public String getExecutionVenue() { return executionVenue; }
    public void setExecutionVenue(String executionVenue) { this.executionVenue = executionVenue; }
    
    public TradeStatus getStatus() { return status; }
    public void setStatus(TradeStatus status) { this.status = status; }
    
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    
    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    
    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
    
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    
    public String getStrategyName() { return strategyName; }
    public void setStrategyName(String strategyName) { this.strategyName = strategyName; }
    
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    
    public String getExecutionQuality() { return executionQuality; }
    public void setExecutionQuality(String executionQuality) { this.executionQuality = executionQuality; }
}