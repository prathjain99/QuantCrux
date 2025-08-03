package com.quantcrux.dto;

import com.quantcrux.model.AssetType;

import java.math.BigDecimal;

public class SymbolSearchResponse {
    
    private String symbol;
    private String name;
    private String exchange;
    private String currency;
    private AssetType assetType;
    private String sector;
    private String industry;
    private String country;
    private Boolean isTradeable;
    private BigDecimal marketCap;
    private String description;
    
    // Constructors
    public SymbolSearchResponse() {}
    
    public SymbolSearchResponse(String symbol, String name, AssetType assetType) {
        this.symbol = symbol;
        this.name = name;
        this.assetType = assetType;
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }
    
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public Boolean getIsTradeable() { return isTradeable; }
    public void setIsTradeable(Boolean isTradeable) { this.isTradeable = isTradeable; }
    
    public BigDecimal getMarketCap() { return marketCap; }
    public void setMarketCap(BigDecimal marketCap) { this.marketCap = marketCap; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}