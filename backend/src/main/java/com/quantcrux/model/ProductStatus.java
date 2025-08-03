package com.quantcrux.model;

public enum ProductStatus {
    DRAFT("Draft"),
    ISSUED("Issued"),
    ACTIVE("Active"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    ProductStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}