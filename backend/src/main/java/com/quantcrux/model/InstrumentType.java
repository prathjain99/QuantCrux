package com.quantcrux.model;

public enum InstrumentType {
    ASSET("Asset"),
    STRATEGY("Strategy"),
    PRODUCT("Product");
    
    private final String displayName;
    
    InstrumentType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}