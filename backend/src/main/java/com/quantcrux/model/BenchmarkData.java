package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "benchmark_data", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"symbol", "date"}))
public class BenchmarkData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotNull
    @Column(nullable = false, length = 20)
    private String symbol;
    
    @NotNull
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate date;
    
    @NotNull
    @Column(name = "close_price", precision = 15, scale = 6, nullable = false)
    private BigDecimal closePrice;
    
    // Additional metrics
    @Column(name = "total_return_index", precision = 15, scale = 6)
    private BigDecimal totalReturnIndex;
    
    @Column(name = "dividend_yield", precision = 8, scale = 6)
    private BigDecimal dividendYield;
    
    @Column(name = "pe_ratio", precision = 8, scale = 2)
    private BigDecimal peRatio;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public BenchmarkData() {}
    
    public BenchmarkData(String symbol, String name, LocalDate date, BigDecimal closePrice) {
        this.symbol = symbol;
        this.name = name;
        this.date = date;
        this.closePrice = closePrice;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }
    
    public BigDecimal getTotalReturnIndex() { return totalReturnIndex; }
    public void setTotalReturnIndex(BigDecimal totalReturnIndex) { this.totalReturnIndex = totalReturnIndex; }
    
    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }
    
    public BigDecimal getPeRatio() { return peRatio; }
    public void setPeRatio(BigDecimal peRatio) { this.peRatio = peRatio; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}