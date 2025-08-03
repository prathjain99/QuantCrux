package com.quantcrux.model;

public enum OrderStatus {
    PENDING("Pending"),
    SUBMITTED("Submitted"),
    PARTIALLY_FILLED("Partially Filled"),
    FILLED("Filled"),
    CANCELLED("Cancelled"),
    REJECTED("Rejected"),
    EXPIRED("Expired");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}