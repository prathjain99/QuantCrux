package com.quantcrux.model;

public enum PortfolioStatus {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    ARCHIVED("Archived");
    
    private final String displayName;
    
    PortfolioStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}