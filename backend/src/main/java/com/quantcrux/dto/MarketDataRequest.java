package com.quantcrux.dto;

import com.quantcrux.model.DataType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class MarketDataRequest {
    
    @NotBlank
    private String symbol;
    
    private DataType dataType = DataType.LIVE_PRICE;
    
    private String timeframe; // For OHLCV data
    
    private LocalDateTime startTime; // For historical data
    
    private LocalDateTime endTime; // For historical data
    
    private Integer limit = 100; // Max data points to return
    
    private String preferredSource; // Optional source preference
    
    private Boolean forceRefresh = false; // Skip cache
    
    // Constructors
    public MarketDataRequest() {}
    
    public MarketDataRequest(String symbol) {
        this.symbol = symbol;
    }
    
    public MarketDataRequest(String symbol, DataType dataType) {
        this.symbol = symbol;
        this.dataType = dataType;
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    
    public String getPreferredSource() { return preferredSource; }
    public void setPreferredSource(String preferredSource) { this.preferredSource = preferredSource; }
    
    public Boolean getForceRefresh() { return forceRefresh; }
    public void setForceRefresh(Boolean forceRefresh) { this.forceRefresh = forceRefresh; }
}