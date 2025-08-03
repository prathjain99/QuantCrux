package com.quantcrux.model;

public enum PricingModel {
    BLACK_SCHOLES("Black-Scholes"),
    MONTE_CARLO("Monte Carlo"),
    BINOMIAL_TREE("Binomial Tree"),
    CUSTOM("Custom");
    
    private final String displayName;
    
    PricingModel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}