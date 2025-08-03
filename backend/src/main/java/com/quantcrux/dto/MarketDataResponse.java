package com.quantcrux.dto;

import com.quantcrux.model.DataType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MarketDataResponse {
    
    private String symbol;
    private DataType dataType;
    private String timeframe;
    
    // Single price data (for live prices)
    private BigDecimal price;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private BigDecimal dayChange;
    private BigDecimal dayChangePercent;
    private BigDecimal volume;
    
    // OHLCV data (for historical/intraday)
    private List<OHLCVData> ohlcvData;
    
    // Metadata
    private LocalDateTime dataTimestamp;
    private String source;
    private Integer qualityScore;
    private Boolean isStale;
    private String message;
    
    // Nested class for OHLCV data points
    public static class OHLCVData {
        private LocalDateTime timestamp;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private BigDecimal volume;
        
        public OHLCVData() {}
        
        public OHLCVData(LocalDateTime timestamp, BigDecimal open, BigDecimal high, 
                        BigDecimal low, BigDecimal close, BigDecimal volume) {
            this.timestamp = timestamp;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
        }
        
        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public BigDecimal getOpen() { return open; }
        public void setOpen(BigDecimal open) { this.open = open; }
        
        public BigDecimal getHigh() { return high; }
        public void setHigh(BigDecimal high) { this.high = high; }
        
        public BigDecimal getLow() { return low; }
        public void setLow(BigDecimal low) { this.low = low; }
        
        public BigDecimal getClose() { return close; }
        public void setClose(BigDecimal close) { this.close = close; }
        
        public BigDecimal getVolume() { return volume; }
        public void setVolume(BigDecimal volume) { this.volume = volume; }
    }
    
    // Constructors
    public MarketDataResponse() {}
    
    public MarketDataResponse(String symbol, BigDecimal price, String source) {
        this.symbol = symbol;
        this.price = price;
        this.source = source;
        this.dataType = DataType.LIVE_PRICE;
        this.dataTimestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }
    
    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }
    
    public BigDecimal getDayChange() { return dayChange; }
    public void setDayChange(BigDecimal dayChange) { this.dayChange = dayChange; }
    
    public BigDecimal getDayChangePercent() { return dayChangePercent; }
    public void setDayChangePercent(BigDecimal dayChangePercent) { this.dayChangePercent = dayChangePercent; }
    
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }
    
    public List<OHLCVData> getOhlcvData() { return ohlcvData; }
    public void setOhlcvData(List<OHLCVData> ohlcvData) { this.ohlcvData = ohlcvData; }
    
    public LocalDateTime getDataTimestamp() { return dataTimestamp; }
    public void setDataTimestamp(LocalDateTime dataTimestamp) { this.dataTimestamp = dataTimestamp; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Integer getQualityScore() { return qualityScore; }
    public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }
    
    public Boolean getIsStale() { return isStale; }
    public void setIsStale(Boolean isStale) { this.isStale = isStale; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}