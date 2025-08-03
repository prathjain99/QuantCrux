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
@Table(name = "market_quotes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"symbol", "instrument_type", "market_date"}))
public class MarketQuote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotNull
    @Column(nullable = false, length = 50)
    private String symbol;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false)
    private InstrumentType instrumentType;
    
    // Price data
    @Column(name = "bid_price", precision = 15, scale = 6)
    private BigDecimal bidPrice;
    
    @Column(name = "ask_price", precision = 15, scale = 6)
    private BigDecimal askPrice;
    
    @NotNull
    @Column(name = "last_price", precision = 15, scale = 6, nullable = false)
    private BigDecimal lastPrice;
    
    @Column(precision = 20, scale = 2)
    private BigDecimal volume;
    
    // Market data
    @Column(name = "open_price", precision = 15, scale = 6)
    private BigDecimal openPrice;
    
    @Column(name = "high_price", precision = 15, scale = 6)
    private BigDecimal highPrice;
    
    @Column(name = "low_price", precision = 15, scale = 6)
    private BigDecimal lowPrice;
    
    @Column(name = "prev_close", precision = 15, scale = 6)
    private BigDecimal prevClose;
    
    // Timestamps
    @NotNull
    @Column(name = "quote_time", nullable = false)
    private LocalDateTime quoteTime = LocalDateTime.now();
    
    @NotNull
    @Column(name = "market_date", nullable = false)
    private LocalDate marketDate = LocalDate.now();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public MarketQuote() {}
    
    public MarketQuote(String symbol, InstrumentType instrumentType, BigDecimal lastPrice) {
        this.symbol = symbol;
        this.instrumentType = instrumentType;
        this.lastPrice = lastPrice;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public InstrumentType getInstrumentType() { return instrumentType; }
    public void setInstrumentType(InstrumentType instrumentType) { this.instrumentType = instrumentType; }
    
    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }
    
    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }
    
    public BigDecimal getLastPrice() { return lastPrice; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }
    
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }
    
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    
    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
    
    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
    
    public BigDecimal getPrevClose() { return prevClose; }
    public void setPrevClose(BigDecimal prevClose) { this.prevClose = prevClose; }
    
    public LocalDateTime getQuoteTime() { return quoteTime; }
    public void setQuoteTime(LocalDateTime quoteTime) { this.quoteTime = quoteTime; }
    
    public LocalDate getMarketDate() { return marketDate; }
    public void setMarketDate(LocalDate marketDate) { this.marketDate = marketDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}