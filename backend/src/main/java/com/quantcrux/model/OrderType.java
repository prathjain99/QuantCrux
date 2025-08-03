package com.quantcrux.model;

public enum OrderType {
    MARKET("Market"),
    LIMIT("Limit"),
    STOP("Stop"),
    STOP_LIMIT("Stop Limit"),
    CONDITIONAL("Conditional");
    
    private final String displayName;
    
    OrderType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}