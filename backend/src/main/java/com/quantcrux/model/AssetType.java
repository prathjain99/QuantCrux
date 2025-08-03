// package com.quantcrux.model;
// import com.fasterxml.jackson.annotation.JsonCreator;
// import com.fasterxml.jackson.annotation.JsonValue;

// public enum AssetType {
//     STOCK("Stock"),
//     CRYPTO("Cryptocurrency"),
//     ETF("ETF"),
//     FOREX("Forex"),
//     INDEX("Index"),
//     COMMODITY("Commodity");
    
//     private final String displayName;
    
//     AssetType(String displayName) {
//         this.displayName = displayName;
//     }
    
//     public String getDisplayName() {
//         return displayName;
//     }
// }

package com.quantcrux.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static AssetType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (AssetType type : AssetType.values()) {
            if (type.name().equalsIgnoreCase(value) || type.displayName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AssetType: " + value);
    }
}
