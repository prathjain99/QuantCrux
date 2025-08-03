package com.quantcrux.model;

public enum TradeStatus {
    EXECUTED("Executed"),
    SETTLED("Settled"),
    FAILED("Failed"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    TradeStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}