package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_payoffs")
public class ProductPayoff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotNull
    @Column(name = "spot_price", precision = 15, scale = 6, nullable = false)
    private BigDecimal spotPrice;
    
    @NotNull
    @Column(name = "payoff_value", precision = 15, scale = 6, nullable = false)
    private BigDecimal payoffValue;
    
    @Column(precision = 8, scale = 6)
    private BigDecimal probability;
    
    @Column(name = "scenario_type", length = 50)
    private String scenarioType = "base";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public ProductPayoff() {}
    
    public ProductPayoff(Product product, BigDecimal spotPrice, BigDecimal payoffValue) {
        this.product = product;
        this.spotPrice = spotPrice;
        this.payoffValue = payoffValue;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    public BigDecimal getSpotPrice() { return spotPrice; }
    public void setSpotPrice(BigDecimal spotPrice) { this.spotPrice = spotPrice; }
    
    public BigDecimal getPayoffValue() { return payoffValue; }
    public void setPayoffValue(BigDecimal payoffValue) { this.payoffValue = payoffValue; }
    
    public BigDecimal getProbability() { return probability; }
    public void setProbability(BigDecimal probability) { this.probability = probability; }
    
    public String getScenarioType() { return scenarioType; }
    public void setScenarioType(String scenarioType) { this.scenarioType = scenarioType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}