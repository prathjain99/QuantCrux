package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "symbol_metadata")
public class SymbolMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotBlank
    @Column(unique = true, nullable = false, length = 20)
    private String symbol;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column(length = 50)
    private String exchange;
    
    @Column(length = 10)
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;
    
    @Column(length = 50)
    private String sector;
    
    @Column(length = 100)
    private String industry;
    
    @Column(length = 50)
    private String country;
    
    // Trading info
    @Column(name = "is_tradeable")
    private Boolean isTradeable = true;
    
    @Column(name = "min_quantity", precision = 20, scale = 8)
    private BigDecimal minQuantity = BigDecimal.ONE;
    
    @Column(name = "tick_size", precision = 15, scale = 6)
    private BigDecimal tickSize = BigDecimal.valueOf(0.01);
    
    // Metadata
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column
    private String website;
    
    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public SymbolMetadata() {}
    
    public SymbolMetadata(String symbol, String name, AssetType assetType) {
        this.symbol = symbol;
        this.name = name;
        this.assetType = assetType;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
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
    
    public BigDecimal getMinQuantity() { return minQuantity; }
    public void setMinQuantity(BigDecimal minQuantity) { this.minQuantity = minQuantity; }
    
    public BigDecimal getTickSize() { return tickSize; }
    public void setTickSize(BigDecimal tickSize) { this.tickSize = tickSize; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public BigDecimal getMarketCap() { return marketCap; }
    public void setMarketCap(BigDecimal marketCap) { this.marketCap = marketCap; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}