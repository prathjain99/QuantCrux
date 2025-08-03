package com.quantcrux.dto;

import com.quantcrux.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderRequest {
    
    @NotNull
    private UUID portfolioId;
    
    private UUID instrumentId;
    
    @NotNull
    private InstrumentType instrumentType;
    
    @NotBlank
    private String symbol;
    
    @NotNull
    private OrderSide side;
    
    @NotNull
    private OrderType orderType;
    
    @NotNull
    private BigDecimal quantity;
    
    private BigDecimal limitPrice;
    
    private BigDecimal stopPrice;
    
    private TimeInForce timeInForce = TimeInForce.DAY;
    
    private LocalDateTime expiresAt;
    
    private String notes;
    
    private String clientOrderId;
    
    // Constructors
    public OrderRequest() {}
    
    public OrderRequest(UUID portfolioId, InstrumentType instrumentType, String symbol, 
                       OrderSide side, OrderType orderType, BigDecimal quantity) {
        this.portfolioId = portfolioId;
        this.instrumentType = instrumentType;
        this.symbol = symbol;
        this.side = side;
        this.orderType = orderType;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }
    
    public UUID getInstrumentId() { return instrumentId; }
    public void setInstrumentId(UUID instrumentId) { this.instrumentId = instrumentId; }
    
    public InstrumentType getInstrumentType() { return instrumentType; }
    public void setInstrumentType(InstrumentType instrumentType) { this.instrumentType = instrumentType; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }
    
    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getLimitPrice() { return limitPrice; }
    public void setLimitPrice(BigDecimal limitPrice) { this.limitPrice = limitPrice; }
    
    public BigDecimal getStopPrice() { return stopPrice; }
    public void setStopPrice(BigDecimal stopPrice) { this.stopPrice = stopPrice; }
    
    public TimeInForce getTimeInForce() { return timeInForce; }
    public void setTimeInForce(TimeInForce timeInForce) { this.timeInForce = timeInForce; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getClientOrderId() { return clientOrderId; }
    public void setClientOrderId(String clientOrderId) { this.clientOrderId = clientOrderId; }
}