package com.quantcrux.model;

public enum StrategyStatus {
    DRAFT("Draft"),
    ACTIVE("Active"),
    PAUSED("Paused"),
    ARCHIVED("Archived");
    
    private final String displayName;
    
    StrategyStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}