package com.quantcrux.model;

public enum ProductType {
    DIGITAL_OPTION("Digital Option"),
    BARRIER_OPTION("Barrier Option"),
    KNOCK_IN_OPTION("Knock-In Option"),
    KNOCK_OUT_OPTION("Knock-Out Option"),
    DUAL_CURRENCY("Dual Currency"),
    STRATEGY_LINKED_NOTE("Strategy-Linked Note"),
    CUSTOM_PAYOFF("Custom Payoff");
    
    private final String displayName;
    
    ProductType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}