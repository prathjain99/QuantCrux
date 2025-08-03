package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_data", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"symbol", "timeframe", "timestamp"}))
public class MarketData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotNull
    @Column(nullable = false, length = 20)
    private String symbol;
    
    @NotNull
    @Column(nullable = false, length = 10)
    private String timeframe;
    
    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @NotNull
    @Column(name = "open_price", precision = 15, scale = 6, nullable = false)
    private BigDecimal openPrice;
    
    @NotNull
    @Column(name = "high_price", precision = 15, scale = 6, nullable = false)
    private BigDecimal highPrice;
    
    @NotNull
    @Column(name = "low_price", precision = 15, scale = 6, nullable = false)
    private BigDecimal lowPrice;
    
    @NotNull
    @Column(name = "close_price", precision = 15, scale = 6, nullable = false)
    private BigDecimal closePrice;
    
    @NotNull
    @Column(precision = 20, scale = 2, nullable = false)
    private BigDecimal volume;
    
    @Column(columnDefinition = "TEXT")
    private String indicators;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public MarketData() {}
    
    public MarketData(String symbol, String timeframe, LocalDateTime timestamp,
                     BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice,
                     BigDecimal closePrice, BigDecimal volume) {
        this.symbol = symbol;
        this.timeframe = timeframe;
        this.timestamp = timestamp;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    
    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
    
    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
    
    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }
    
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }
    
    public String getIndicators() { return indicators; }
    public void setIndicators(String indicators) { this.indicators = indicators; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}