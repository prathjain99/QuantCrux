package com.quantcrux.dto;

import com.quantcrux.model.PortfolioStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PortfolioResponse {
    
    private UUID id;
    private String name;
    private String description;
    private String ownerName;
    private String managerName;
    private UUID ownerId;
    private UUID managerId;
    
    private BigDecimal initialCapital;
    private BigDecimal currentNav;
    private BigDecimal cashBalance;
    private BigDecimal totalPnl;
    private BigDecimal totalReturnPct;
    
    // Risk metrics
    private BigDecimal var95;
    private BigDecimal volatility;
    private BigDecimal beta;
    private BigDecimal maxDrawdown;
    private BigDecimal sharpeRatio;
    
    // Status and settings
    private PortfolioStatus status;
    private String currency;
    private String benchmarkSymbol;
    
    // Holdings summary
    private List<HoldingResponse> holdings;
    private List<NAVPoint> navHistory;
    private List<TransactionResponse> recentTransactions;
    
    // Performance breakdown
    private List<AllocationBreakdown> assetAllocation;
    private List<AllocationBreakdown> sectorAllocation;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Nested classes
    public static class HoldingResponse {
        private UUID id;
        private String instrumentType;
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal avgPrice;
        private BigDecimal latestPrice;
        private BigDecimal marketValue;
        private BigDecimal costBasis;
        private BigDecimal unrealizedPnl;
        private BigDecimal realizedPnl;
        private String sector;
        private String assetClass;
        private BigDecimal weightPct;
        
        // Constructors
        public HoldingResponse() {}
        
        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public String getInstrumentType() { return instrumentType; }
        public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        
        public BigDecimal getAvgPrice() { return avgPrice; }
        public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }
        
        public BigDecimal getLatestPrice() { return latestPrice; }
        public void setLatestPrice(BigDecimal latestPrice) { this.latestPrice = latestPrice; }
        
        public BigDecimal getMarketValue() { return marketValue; }
        public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }
        
        public BigDecimal getCostBasis() { return costBasis; }
        public void setCostBasis(BigDecimal costBasis) { this.costBasis = costBasis; }
        
        public BigDecimal getUnrealizedPnl() { return unrealizedPnl; }
        public void setUnrealizedPnl(BigDecimal unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }
        
        public BigDecimal getRealizedPnl() { return realizedPnl; }
        public void setRealizedPnl(BigDecimal realizedPnl) { this.realizedPnl = realizedPnl; }
        
        public String getSector() { return sector; }
        public void setSector(String sector) { this.sector = sector; }
        
        public String getAssetClass() { return assetClass; }
        public void setAssetClass(String assetClass) { this.assetClass = assetClass; }
        
        public BigDecimal getWeightPct() { return weightPct; }
        public void setWeightPct(BigDecimal weightPct) { this.weightPct = weightPct; }
    }
    
    public static class NAVPoint {
        private String date;
        private BigDecimal nav;
        private BigDecimal dailyReturn;
        
        public NAVPoint() {}
        
        public NAVPoint(String date, BigDecimal nav) {
            this.date = date;
            this.nav = nav;
        }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public BigDecimal getNav() { return nav; }
        public void setNav(BigDecimal nav) { this.nav = nav; }
        
        public BigDecimal getDailyReturn() { return dailyReturn; }
        public void setDailyReturn(BigDecimal dailyReturn) { this.dailyReturn = dailyReturn; }
    }
    
    public static class TransactionResponse {
        private UUID id;
        private String transactionType;
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal amount;
        private String description;
        private LocalDateTime executedAt;
        
        public TransactionResponse() {}
        
        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getExecutedAt() { return executedAt; }
        public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    }
    
    public static class AllocationBreakdown {
        private String category;
        private BigDecimal value;
        private BigDecimal percentage;
        private BigDecimal pnl;
        
        public AllocationBreakdown() {}
        
        public AllocationBreakdown(String category, BigDecimal value, BigDecimal percentage) {
            this.category = category;
            this.value = value;
            this.percentage = percentage;
        }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
        
        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
        
        public BigDecimal getPnl() { return pnl; }
        public void setPnl(BigDecimal pnl) { this.pnl = pnl; }
    }
    
    // Constructors
    public PortfolioResponse() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    
    public UUID getManagerId() { return managerId; }
    public void setManagerId(UUID managerId) { this.managerId = managerId; }
    
    public BigDecimal getInitialCapital() { return initialCapital; }
    public void setInitialCapital(BigDecimal initialCapital) { this.initialCapital = initialCapital; }
    
    public BigDecimal getCurrentNav() { return currentNav; }
    public void setCurrentNav(BigDecimal currentNav) { this.currentNav = currentNav; }
    
    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }
    
    public BigDecimal getTotalPnl() { return totalPnl; }
    public void setTotalPnl(BigDecimal totalPnl) { this.totalPnl = totalPnl; }
    
    public BigDecimal getTotalReturnPct() { return totalReturnPct; }
    public void setTotalReturnPct(BigDecimal totalReturnPct) { this.totalReturnPct = totalReturnPct; }
    
    public BigDecimal getVar95() { return var95; }
    public void setVar95(BigDecimal var95) { this.var95 = var95; }
    
    public BigDecimal getVolatility() { return volatility; }
    public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }
    
    public BigDecimal getBeta() { return beta; }
    public void setBeta(BigDecimal beta) { this.beta = beta; }
    
    public BigDecimal getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
    
    public BigDecimal getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
    
    public PortfolioStatus getStatus() { return status; }
    public void setStatus(PortfolioStatus status) { this.status = status; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getBenchmarkSymbol() { return benchmarkSymbol; }
    public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
    
    public List<HoldingResponse> getHoldings() { return holdings; }
    public void setHoldings(List<HoldingResponse> holdings) { this.holdings = holdings; }
    
    public List<NAVPoint> getNavHistory() { return navHistory; }
    public void setNavHistory(List<NAVPoint> navHistory) { this.navHistory = navHistory; }
    
    public List<TransactionResponse> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<TransactionResponse> recentTransactions) { this.recentTransactions = recentTransactions; }
    
    public List<AllocationBreakdown> getAssetAllocation() { return assetAllocation; }
    public void setAssetAllocation(List<AllocationBreakdown> assetAllocation) { this.assetAllocation = assetAllocation; }
    
    public List<AllocationBreakdown> getSectorAllocation() { return sectorAllocation; }
    public void setSectorAllocation(List<AllocationBreakdown> sectorAllocation) { this.sectorAllocation = sectorAllocation; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}