package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;
    
    @NotBlank
    @Column(name = "underlying_asset", nullable = false)
    private String underlyingAsset;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_strategy_id")
    private Strategy linkedStrategy;
    
    // Product terms
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal notional = BigDecimal.valueOf(100000.00);
    
    @Column(name = "strike_price", precision = 15, scale = 6)
    private BigDecimal strikePrice;
    
    @Column(name = "barrier_level", precision = 15, scale = 6)
    private BigDecimal barrierLevel;
    
    @Column(name = "payoff_rate", precision = 8, scale = 6)
    private BigDecimal payoffRate;
    
    // Dates
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    @NotNull
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;
    
    @Column(name = "settlement_date")
    private LocalDate settlementDate;
    
    // Configuration
    @NotNull
    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model", nullable = false)
    private PricingModel pricingModel = PricingModel.MONTE_CARLO;
    
    // Pricing results
    @Column(name = "fair_value", precision = 15, scale = 2)
    private BigDecimal fairValue;
    
    @Column(name = "implied_volatility", precision = 8, scale = 6)
    private BigDecimal impliedVolatility;
    
    // Greeks
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
    
    // Status and metadata
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;
    
    @Column(name = "current_version")
    private Integer currentVersion = 1;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductVersion> versions;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductPricing> pricings;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductPayoff> payoffs;
    
    // Constructors
    public Product() {}
    
    public Product(User user, String name, ProductType productType, String underlyingAsset, String configJson) {
        this.user = user;
        this.name = name;
        this.productType = productType;
        this.underlyingAsset = underlyingAsset;
        this.configJson = configJson;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }
    
    public String getUnderlyingAsset() { return underlyingAsset; }
    public void setUnderlyingAsset(String underlyingAsset) { this.underlyingAsset = underlyingAsset; }
    
    public Strategy getLinkedStrategy() { return linkedStrategy; }
    public void setLinkedStrategy(Strategy linkedStrategy) { this.linkedStrategy = linkedStrategy; }
    
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    
    public List<ProductVersion> getVersions() { return versions; }
    public void setVersions(List<ProductVersion> versions) { this.versions = versions; }
    
    public List<ProductPricing> getPricings() { return pricings; }
    public void setPricings(List<ProductPricing> pricings) { this.pricings = pricings; }
    
    public List<ProductPayoff> getPayoffs() { return payoffs; }
    public void setPayoffs(List<ProductPayoff> payoffs) { this.payoffs = payoffs; }
}