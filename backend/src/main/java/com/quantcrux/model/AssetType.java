package com.quantcrux.model;

public enum AssetType {
    STOCK("Stock"),
    CRYPTO("Cryptocurrency"),
    ETF("ETF"),
    FOREX("Forex"),
    INDEX("Index"),
    COMMODITY("Commodity");
    
    private final String displayName;
    
    AssetType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}