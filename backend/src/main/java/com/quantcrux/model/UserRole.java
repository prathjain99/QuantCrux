package com.quantcrux.model;

public enum UserRole {
    CLIENT("Client"),
    PORTFOLIO_MANAGER("Portfolio Manager"),
    RESEARCHER("Researcher"),
    ADMIN("Administrator");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}