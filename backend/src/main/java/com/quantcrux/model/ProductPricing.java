package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_pricings")
public class ProductPricing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotNull
    @Column(name = "pricing_date", nullable = false)
    private LocalDateTime pricingDate = LocalDateTime.now();
    
    @NotNull
    @Column(name = "fair_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal fairValue;
    
    @Column(name = "implied_volatility", precision = 8, scale = 6)
    private BigDecimal impliedVolatility;
    
    // Greeks at pricing time
    @Column(name = "delta_value", precision = 10, scale = 6)
    private BigDecimal deltaValue;
    
    @Column(name = "gamma_value", precision = 10, scale = 6)
    private BigDecimal gammaValue;
    
    @Column(name = "theta_value", precision = 10, scale = 6)
    private BigDecimal thetaValue;
    
    @Column(name = "vega_value", precision = 10, scale = 6)
    private BigDecimal vegaValue;
    
    @Column(name = "rho_value", precision = 10, scale = 6)
    private BigDecimal rhoValue;
    
    // Market conditions
    @Column(name = "underlying_price", precision = 15, scale = 6)
    private BigDecimal underlyingPrice;
    
    @Column(name = "risk_free_rate", precision = 8, scale = 6)
    private BigDecimal riskFreeRate;
    
    @Column(name = "time_to_maturity", precision = 10, scale = 6)
    private BigDecimal timeToMaturity;
    
    // Pricing parameters
    @Column(name = "simulation_runs")
    private Integer simulationRuns;
    
    @Column(name = "pricing_model", length = 50)
    private String pricingModelUsed;
    
    @Column(name = "model_parameters", columnDefinition = "TEXT")
    private String modelParameters;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public ProductPricing() {}
    
    public ProductPricing(Product product, BigDecimal fairValue) {
        this.product = product;
        this.fairValue = fairValue;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    public LocalDateTime getPricingDate() { return pricingDate; }
    public void setPricingDate(LocalDateTime pricingDate) { this.pricingDate = pricingDate; }
    
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
    
    public BigDecimal getUnderlyingPrice() { return underlyingPrice; }
    public void setUnderlyingPrice(BigDecimal underlyingPrice) { this.underlyingPrice = underlyingPrice; }
    
    public BigDecimal getRiskFreeRate() { return riskFreeRate; }
    public void setRiskFreeRate(BigDecimal riskFreeRate) { this.riskFreeRate = riskFreeRate; }
    
    public BigDecimal getTimeToMaturity() { return timeToMaturity; }
    public void setTimeToMaturity(BigDecimal timeToMaturity) { this.timeToMaturity = timeToMaturity; }
    
    public Integer getSimulationRuns() { return simulationRuns; }
    public void setSimulationRuns(Integer simulationRuns) { this.simulationRuns = simulationRuns; }
    
    public String getPricingModelUsed() { return pricingModelUsed; }
    public void setPricingModelUsed(String pricingModelUsed) { this.pricingModelUsed = pricingModelUsed; }
    
    public String getModelParameters() { return modelParameters; }
    public void setModelParameters(String modelParameters) { this.modelParameters = modelParameters; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}