package com.quantcrux.model;

public enum AccountStatus {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    PENDING_VERIFICATION("Pending Verification");
    
    private final String displayName;
    
    AccountStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}