package com.quantcrux.model;

public enum SignalType {
    BUY("Buy"),
    SELL("Sell"),
    HOLD("Hold"),
    NO_SIGNAL("No Signal");
    
    private final String displayName;
    
    SignalType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}