package com.quantcrux.dto;

import com.quantcrux.model.PricingModel;
import com.quantcrux.model.ProductStatus;
import com.quantcrux.model.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ProductRequest {
    
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    private ProductType productType;
    
    @NotBlank
    private String underlyingAsset;
    
    private UUID linkedStrategyId;
    
    // Product terms
    private BigDecimal notional = BigDecimal.valueOf(100000.00);
    private BigDecimal strikePrice;
    private BigDecimal barrierLevel;
    private BigDecimal payoffRate;
    
    // Dates
    private LocalDate issueDate;
    
    @NotNull
    private LocalDate maturityDate;
    
    private LocalDate settlementDate;
    
    // Configuration
    @NotNull
    private String configJson;
    
    private PricingModel pricingModel = PricingModel.MONTE_CARLO;
    private ProductStatus status = ProductStatus.DRAFT;
    
    // Pricing parameters
    private Integer simulationRuns = 10000;
    private BigDecimal riskFreeRate = BigDecimal.valueOf(0.05);
    private BigDecimal impliedVolatility = BigDecimal.valueOf(0.20);
    
    // Constructors
    public ProductRequest() {}
    
    public ProductRequest(String name, ProductType productType, String underlyingAsset, String configJson, LocalDate maturityDate) {
        this.name = name;
        this.productType = productType;
        this.underlyingAsset = underlyingAsset;
        this.configJson = configJson;
        this.maturityDate = maturityDate;
    }
    
    // Getters and Setters
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
    
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    
    public Integer getSimulationRuns() { return simulationRuns; }
    public void setSimulationRuns(Integer simulationRuns) { this.simulationRuns = simulationRuns; }
    
    public BigDecimal getRiskFreeRate() { return riskFreeRate; }
    public void setRiskFreeRate(BigDecimal riskFreeRate) { this.riskFreeRate = riskFreeRate; }
    
    public BigDecimal getImpliedVolatility() { return impliedVolatility; }
    public void setImpliedVolatility(BigDecimal impliedVolatility) { this.impliedVolatility = impliedVolatility; }
}