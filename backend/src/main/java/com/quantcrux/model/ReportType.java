package com.quantcrux.model;

public enum ReportType {
    PORTFOLIO_SUMMARY("Portfolio Summary"),
    RISK_ANALYSIS("Risk Analysis"),
    PERFORMANCE_REPORT("Performance Report"),
    TRADE_BLOTTER("Trade Blotter"),
    ATTRIBUTION_ANALYSIS("Attribution Analysis"),
    CORRELATION_REPORT("Correlation Report"),
    CUSTOM("Custom Report");
    
    private final String displayName;
    
    ReportType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}