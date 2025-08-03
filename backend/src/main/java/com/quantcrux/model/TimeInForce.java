package com.quantcrux.model;

public enum TimeInForce {
    DAY("Day"),
    GTC("Good Till Cancelled"),
    IOC("Immediate or Cancel"),
    FOK("Fill or Kill");
    
    private final String displayName;
    
    TimeInForce(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}