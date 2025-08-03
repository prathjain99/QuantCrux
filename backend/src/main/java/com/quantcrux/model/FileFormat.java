package com.quantcrux.model;

public enum FileFormat {
    PDF("PDF"),
    CSV("CSV"),
    XLS("Excel");
    
    private final String displayName;
    
    FileFormat(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}