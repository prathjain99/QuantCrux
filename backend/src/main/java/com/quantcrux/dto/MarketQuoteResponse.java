package com.quantcrux.dto;

import com.quantcrux.model.InstrumentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketQuoteResponse {
    
    private String symbol;
    private InstrumentType instrumentType;
    
    // Price data
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private BigDecimal lastPrice;
    private BigDecimal volume;
    
    // Market data
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal prevClose;
    
    // Calculated fields
    private BigDecimal dayChange;
    private BigDecimal dayChangePercent;
    private BigDecimal spread;
    private String trend; // "UP", "DOWN", "FLAT"
    
    private LocalDateTime quoteTime;
    private LocalDateTime updatedAt;
    
    // Constructors
    public MarketQuoteResponse() {}
    
    public MarketQuoteResponse(String symbol, InstrumentType instrumentType, BigDecimal lastPrice) {
        this.symbol = symbol;
        this.instrumentType = instrumentType;
        this.lastPrice = lastPrice;
    }
    
    // Getters and Setters
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
    
    public BigDecimal getDayChange() { return dayChange; }
    public void setDayChange(BigDecimal dayChange) { this.dayChange = dayChange; }
    
    public BigDecimal getDayChangePercent() { return dayChangePercent; }
    public void setDayChangePercent(BigDecimal dayChangePercent) { this.dayChangePercent = dayChangePercent; }
    
    public BigDecimal getSpread() { return spread; }
    public void setSpread(BigDecimal spread) { this.spread = spread; }
    
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    
    public LocalDateTime getQuoteTime() { return quoteTime; }
    public void setQuoteTime(LocalDateTime quoteTime) { this.quoteTime = quoteTime; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}