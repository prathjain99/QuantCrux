package com.quantcrux.model;

public enum ReportStatus {
    PENDING("Pending"),
    GENERATING("Generating"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    EXPIRED("Expired");
    
    private final String displayName;
    
    ReportStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}