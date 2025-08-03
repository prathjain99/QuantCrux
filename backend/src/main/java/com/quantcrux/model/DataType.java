package com.quantcrux.model;

public enum DataType {
    LIVE_PRICE("Live Price"),
    OHLCV("OHLCV"),
    INTRADAY("Intraday");
    
    private final String displayName;
    
    DataType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}