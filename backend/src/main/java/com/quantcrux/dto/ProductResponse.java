package com.quantcrux.dto;

import com.quantcrux.model.PricingModel;
import com.quantcrux.model.ProductStatus;
import com.quantcrux.model.ProductType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProductResponse {
    
    private UUID id;
    private String name;
    private String description;
    private ProductType productType;
    private String underlyingAsset;
    private UUID linkedStrategyId;
    private String linkedStrategyName;
    
    // Product terms
    private BigDecimal notional;
    private BigDecimal strikePrice;
    private BigDecimal barrierLevel;
    private BigDecimal payoffRate;
    
    // Dates
    private LocalDate issueDate;
    private LocalDate maturityDate;
    private LocalDate settlementDate;
    
    // Configuration
    private String configJson;
    private PricingModel pricingModel;
    
    // Pricing results
    private BigDecimal fairValue;
    private BigDecimal impliedVolatility;
    
    // Greeks
    private BigDecimal deltaValue;
    private BigDecimal gammaValue;
    private BigDecimal thetaValue;
    private BigDecimal vegaValue;
    private BigDecimal rhoValue;
    
    // Status and metadata
    private ProductStatus status;
    private Integer currentVersion;
    private String ownerName;
    
    // Payoff curve
    private List<PayoffPoint> payoffCurve;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime issuedAt;
    
    // Nested class for payoff points
    public static class PayoffPoint {
        private BigDecimal spotPrice;
        private BigDecimal payoffValue;
        private BigDecimal probability;
        
        public PayoffPoint() {}
        
        public PayoffPoint(BigDecimal spotPrice, BigDecimal payoffValue) {
            this.spotPrice = spotPrice;
            this.payoffValue = payoffValue;
        }
        
        public BigDecimal getSpotPrice() { return spotPrice; }
        public void setSpotPrice(BigDecimal spotPrice) { this.spotPrice = spotPrice; }
        
        public BigDecimal getPayoffValue() { return payoffValue; }
        public void setPayoffValue(BigDecimal payoffValue) { this.payoffValue = payoffValue; }
        
        public BigDecimal getProbability() { return probability; }
        public void setProbability(BigDecimal probability) { this.probability = probability; }
    }
    
    // Constructors
    public ProductResponse() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }
    
    public String getUnderlyingAsset() { return underlyingAsset; }
    public void setUnderlyingAsset(String underlyingAsset) { this.underlyingAsset = underlyingAsset; }
    
    public UUID getLinkedStrategyId() { return linkedStrategyId; }
    public void setLinkedStrategyId(UUID linkedStrategyId) { this.linkedStrategyId = linkedStrategyId; }
    
    public String getLinkedStrategyName() { return linkedStrategyName; }
    public void setLinkedStrategyName(String linkedStrategyName) { this.linkedStrategyName = linkedStrategyName; }
    
    public BigDecimal getNotional() { return notional; }
    public void setNotional(BigDecimal notional) { this.notional = notional; }
    
    public BigDecimal getStrikePrice() { return strikePrice; }
    public void setStrikePrice(BigDecimal strikePrice) { this.strikePrice = strikePrice; }
    
    public BigDecimal getBarrierLevel() { return barrierLevel; }
    public void setBarrierLevel(BigDecimal barrierLevel) { this.barrierLevel = barrierLevel; }
    
    public BigDecimal getPayoffRate() { return payoffRate; }
    public void setPayoffRate(BigDecimal payoffRate) { this.payoffRate = payoffRate; }
    
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    
    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
    
    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
    
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    
    public PricingModel getPricingModel() { return pricingModel; }
    public void setPricingModel(PricingModel pricingModel) { this.pricingModel = pricingModel; }
    
    public BigDecimal getFairValue() { return fairValue; }
    public void setFairValue(BigDecimal fairValue) { this.fairValue = fairValue; }
    
    public BigDecimal getImpliedVolatility() { return impliedVolatility; }
    public void setImpliedVolatility(BigDecimal impliedVolatility) { this.impliedVolatility = impliedVolatility; }
    
    public BigDecimal getDeltaValue() { return deltaValue; }
    public void setDeltaValue(BigDecimal deltaValue) { this.deltaValue = deltaValue; }
    
    public BigDecimal getGammaValue() { return gammaValue; }
    public void setGammaValue(BigDecimal gammaValue) { this.gammaValue = gammaValue; }
    
    public BigDecimal getThetaValue() { return thetaValue; }
    public void setThetaValue(BigDecimal thetaValue) { this.thetaValue = thetaValue; }
    
    public BigDecimal getVegaValue() { return vegaValue; }
    public void setVegaValue(BigDecimal vegaValue) { this.vegaValue = vegaValue; }
    
    public BigDecimal getRhoValue() { return rhoValue; }
    public void setRhoValue(BigDecimal rhoValue) { this.rhoValue = rhoValue; }
    
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    
    public Integer getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(Integer currentVersion) { this.currentVersion = currentVersion; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public List<PayoffPoint> getPayoffCurve() { return payoffCurve; }
    public void setPayoffCurve(List<PayoffPoint> payoffCurve) { this.payoffCurve = payoffCurve; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}