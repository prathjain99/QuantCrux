package com.quantcrux.dto;

import com.quantcrux.model.PortfolioStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class PortfolioRequest {
    
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    private BigDecimal initialCapital;
    
    private UUID managerId; // For PM assignment
    
    private PortfolioStatus status = PortfolioStatus.ACTIVE;
    
    private String currency = "USD";
    
    private String benchmarkSymbol = "SPY";
    
    // Constructors
    public PortfolioRequest() {}
    
    public PortfolioRequest(String name, BigDecimal initialCapital) {
        this.name = name;
        this.initialCapital = initialCapital;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getInitialCapital() { return initialCapital; }
    public void setInitialCapital(BigDecimal initialCapital) { this.initialCapital = initialCapital; }
    
    public UUID getManagerId() { return managerId; }
    public void setManagerId(UUID managerId) { this.managerId = managerId; }
    
    public PortfolioStatus getStatus() { return status; }
    public void setStatus(PortfolioStatus status) { this.status = status; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getBenchmarkSymbol() { return benchmarkSymbol; }
    public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
}