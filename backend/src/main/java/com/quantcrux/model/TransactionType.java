package com.quantcrux.model;

public enum TransactionType {
    BUY("Buy"),
    SELL("Sell"),
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    DIVIDEND("Dividend"),
    INTEREST("Interest"),
    STRATEGY_ALLOCATION("Strategy Allocation"),
    PRODUCT_PURCHASE("Product Purchase"),
    FEE("Fee");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}