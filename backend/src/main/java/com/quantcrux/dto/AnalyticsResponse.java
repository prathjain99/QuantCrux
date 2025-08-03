package com.quantcrux.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class AnalyticsResponse {
    
    private UUID portfolioId;
    private UUID strategyId;
    private String portfolioName;
    private String strategyName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String benchmarkSymbol;
    
    // Risk metrics
    private BigDecimal var95;
    private BigDecimal var99;
    private BigDecimal volatility;
    private BigDecimal beta;
    private BigDecimal alpha;
    private BigDecimal sharpeRatio;
    private BigDecimal sortinoRatio;
    private BigDecimal maxDrawdown;
    private Integer maxDrawdownDuration;
    private BigDecimal calmarRatio;
    private BigDecimal informationRatio;
    private BigDecimal correlationToBenchmark;
    private BigDecimal trackingError;
    
    // Performance metrics
    private BigDecimal totalReturn;
    private BigDecimal cagr;
    private BigDecimal annualizedReturn;
    private BigDecimal excessReturn;
    private Integer totalTrades;
    private Integer winningTrades;
    private Integer losingTrades;
    private BigDecimal winRate;
    private BigDecimal avgWin;
    private BigDecimal avgLoss;
    private BigDecimal profitFactor;
    private BigDecimal tradeFrequency;
    private Integer avgHoldingPeriod;
    private BigDecimal turnoverRatio;
    private BigDecimal benchmarkReturn;
    private BigDecimal outperformance;
    
    // Attribution data
    private Map<String, BigDecimal> assetAttribution;
    private Map<String, BigDecimal> sectorAttribution;
    private Map<String, BigDecimal> strategyAttribution;
    private Map<String, BigDecimal> productAttribution;
    
    // Correlation data
    private Map<String, Map<String, BigDecimal>> correlationMatrix;
    private BigDecimal avgCorrelation;
    private BigDecimal maxCorrelation;
    private BigDecimal minCorrelation;
    private BigDecimal diversificationRatio;
    
    // Constructors
    public AnalyticsResponse() {}
    
    // Getters and Setters
    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }
    
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    
    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }
    
    public String getStrategyName() { return strategyName; }
    public void setStrategyName(String strategyName) { this.strategyName = strategyName; }
    
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    
    public String getBenchmarkSymbol() { return benchmarkSymbol; }
    public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
    
    public BigDecimal getVar95() { return var95; }
    public void setVar95(BigDecimal var95) { this.var95 = var95; }
    
    public BigDecimal getVar99() { return var99; }
    public void setVar99(BigDecimal var99) { this.var99 = var99; }
    
    public BigDecimal getVolatility() { return volatility; }
    public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }
    
    public BigDecimal getBeta() { return beta; }
    public void setBeta(BigDecimal beta) { this.beta = beta; }
    
    public BigDecimal getAlpha() { return alpha; }
    public void setAlpha(BigDecimal alpha) { this.alpha = alpha; }
    
    public BigDecimal getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
    
    public BigDecimal getSortinoRatio() { return sortinoRatio; }
    public void setSortinoRatio(BigDecimal sortinoRatio) { this.sortinoRatio = sortinoRatio; }
    
    public BigDecimal getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
    
    public Integer getMaxDrawdownDuration() { return maxDrawdownDuration; }
    public void setMaxDrawdownDuration(Integer maxDrawdownDuration) { this.maxDrawdownDuration = maxDrawdownDuration; }
    
    public BigDecimal getCalmarRatio() { return calmarRatio; }
    public void setCalmarRatio(BigDecimal calmarRatio) { this.calmarRatio = calmarRatio; }
    
    public BigDecimal getInformationRatio() { return informationRatio; }
    public void setInformationRatio(BigDecimal informationRatio) { this.informationRatio = informationRatio; }
    
    public BigDecimal getCorrelationToBenchmark() { return correlationToBenchmark; }
    public void setCorrelationToBenchmark(BigDecimal correlationToBenchmark) { this.correlationToBenchmark = correlationToBenchmark; }
    
    public BigDecimal getTrackingError() { return trackingError; }
    public void setTrackingError(BigDecimal trackingError) { this.trackingError = trackingError; }
    
    public BigDecimal getTotalReturn() { return totalReturn; }
    public void setTotalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; }
    
    public BigDecimal getCagr() { return cagr; }
    public void setCagr(BigDecimal cagr) { this.cagr = cagr; }
    
    public BigDecimal getAnnualizedReturn() { return annualizedReturn; }
    public void setAnnualizedReturn(BigDecimal annualizedReturn) { this.annualizedReturn = annualizedReturn; }
    
    public BigDecimal getExcessReturn() { return excessReturn; }
    public void setExcessReturn(BigDecimal excessReturn) { this.excessReturn = excessReturn; }
    
    public Integer getTotalTrades() { return totalTrades; }
    public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }
    
    public Integer getWinningTrades() { return winningTrades; }
    public void setWinningTrades(Integer winningTrades) { this.winningTrades = winningTrades; }
    
    public Integer getLosingTrades() { return losingTrades; }
    public void setLosingTrades(Integer losingTrades) { this.losingTrades = losingTrades; }
    
    public BigDecimal getWinRate() { return winRate; }
    public void setWinRate(BigDecimal winRate) { this.winRate = winRate; }
    
    public BigDecimal getAvgWin() { return avgWin; }
    public void setAvgWin(BigDecimal avgWin) { this.avgWin = avgWin; }
    
    public BigDecimal getAvgLoss() { return avgLoss; }
    public void setAvgLoss(BigDecimal avgLoss) { this.avgLoss = avgLoss; }
    
    public BigDecimal getProfitFactor() { return profitFactor; }
    public void setProfitFactor(BigDecimal profitFactor) { this.profitFactor = profitFactor; }
    
    public BigDecimal getTradeFrequency() { return tradeFrequency; }
    public void setTradeFrequency(BigDecimal tradeFrequency) { this.tradeFrequency = tradeFrequency; }
    
    public Integer getAvgHoldingPeriod() { return avgHoldingPeriod; }
    public void setAvgHoldingPeriod(Integer avgHoldingPeriod) { this.avgHoldingPeriod = avgHoldingPeriod; }
    
    public BigDecimal getTurnoverRatio() { return turnoverRatio; }
    public void setTurnoverRatio(BigDecimal turnoverRatio) { this.turnoverRatio = turnoverRatio; }
    
    public BigDecimal getBenchmarkReturn() { return benchmarkReturn; }
    public void setBenchmarkReturn(BigDecimal benchmarkReturn) { this.benchmarkReturn = benchmarkReturn; }
    
    public BigDecimal getOutperformance() { return outperformance; }
    public void setOutperformance(BigDecimal outperformance) { this.outperformance = outperformance; }
    
    public Map<String, BigDecimal> getAssetAttribution() { return assetAttribution; }
    public void setAssetAttribution(Map<String, BigDecimal> assetAttribution) { this.assetAttribution = assetAttribution; }
    
    public Map<String, BigDecimal> getSectorAttribution() { return sectorAttribution; }
    public void setSectorAttribution(Map<String, BigDecimal> sectorAttribution) { this.sectorAttribution = sectorAttribution; }
    
    public Map<String, BigDecimal> getStrategyAttribution() { return strategyAttribution; }
    public void setStrategyAttribution(Map<String, BigDecimal> strategyAttribution) { this.strategyAttribution = strategyAttribution; }
    
    public Map<String, BigDecimal> getProductAttribution() { return productAttribution; }
    public void setProductAttribution(Map<String, BigDecimal> productAttribution) { this.productAttribution = productAttribution; }
    
    public Map<String, Map<String, BigDecimal>> getCorrelationMatrix() { return correlationMatrix; }
    public void setCorrelationMatrix(Map<String, Map<String, BigDecimal>> correlationMatrix) { this.correlationMatrix = correlationMatrix; }
    
    public BigDecimal getAvgCorrelation() { return avgCorrelation; }
    public void setAvgCorrelation(BigDecimal avgCorrelation) { this.avgCorrelation = avgCorrelation; }
    
    public BigDecimal getMaxCorrelation() { return maxCorrelation; }
    public void setMaxCorrelation(BigDecimal maxCorrelation) { this.maxCorrelation = maxCorrelation; }
    
    public BigDecimal getMinCorrelation() { return minCorrelation; }
    public void setMinCorrelation(BigDecimal minCorrelation) { this.minCorrelation = minCorrelation; }
    
    public BigDecimal getDiversificationRatio() { return diversificationRatio; }
    public void setDiversificationRatio(BigDecimal diversificationRatio) { this.diversificationRatio = diversificationRatio; }
}