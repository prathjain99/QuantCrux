package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_data_cache")
public class MarketDataCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotNull
    @Column(nullable = false, length = 20)
    private String symbol;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;
    
    @Column(length = 10)
    private String timeframe;
    
    // Price data
    @Column(precision = 15, scale = 6)
    private BigDecimal price;
    
    @Column(name = "open_price", precision = 15, scale = 6)
    private BigDecimal openPrice;
    
    @Column(name = "high_price", precision = 15, scale = 6)
    private BigDecimal highPrice;
    
    @Column(name = "low_price", precision = 15, scale = 6)
    private BigDecimal lowPrice;
    
    @Column(name = "close_price", precision = 15, scale = 6)
    private BigDecimal closePrice;
    
    @Column(precision = 20, scale = 2)
    private BigDecimal volume;
    
    // Market data
    @Column(name = "bid_price", precision = 15, scale = 6)
    private BigDecimal bidPrice;
    
    @Column(name = "ask_price", precision = 15, scale = 6)
    private BigDecimal askPrice;
    
    @Column(precision = 15, scale = 6)
    private BigDecimal spread;
    
    @Column(name = "day_change", precision = 15, scale = 6)
    private BigDecimal dayChange;
    
    @Column(name = "day_change_percent", precision = 8, scale = 6)
    private BigDecimal dayChangePercent;
    
    // Metadata
    @NotNull
    @Column(name = "data_timestamp", nullable = false)
    private LocalDateTime dataTimestamp;
    
    @NotNull
    @Column(nullable = false, length = 50)
    private String source;
    
    @Column(name = "quality_score")
    private Integer qualityScore = 100;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    // Constructors
    public MarketDataCache() {}
    
    public MarketDataCache(String symbol, DataType dataType, BigDecimal price, String source) {
        this.symbol = symbol;
        this.dataType = dataType;
        this.price = price;
        this.source = source;
        this.dataTimestamp = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(1); // Default 1 minute expiry
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
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
    
    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }
    
    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }
    
    public BigDecimal getSpread() { return spread; }
    public void setSpread(BigDecimal spread) { this.spread = spread; }
    
    public BigDecimal getDayChange() { return dayChange; }
    public void setDayChange(BigDecimal dayChange) { this.dayChange = dayChange; }
    
    public BigDecimal getDayChangePercent() { return dayChangePercent; }
    public void setDayChangePercent(BigDecimal dayChangePercent) { this.dayChangePercent = dayChangePercent; }
    
    public LocalDateTime getDataTimestamp() { return dataTimestamp; }
    public void setDataTimestamp(LocalDateTime dataTimestamp) { this.dataTimestamp = dataTimestamp; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Integer getQualityScore() { return qualityScore; }
    public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}