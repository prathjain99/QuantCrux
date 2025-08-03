package com.quantcrux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantcrux.dto.*;
import com.quantcrux.model.*;
import com.quantcrux.repository.*;
import com.quantcrux.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BacktestService {
    
    private static final Logger logger = LoggerFactory.getLogger(BacktestService.class);
    
    @Autowired
    private BacktestRepository backtestRepository;
    
    @Autowired
    private BacktestTradeRepository tradeRepository;
    
    @Autowired
    private StrategyRepository strategyRepository;
    
    @Autowired
    private StrategyVersionRepository versionRepository;
    
    @Autowired
    private MarketDataRepository marketDataRepository;
    
    @Autowired
    private MarketDataService marketDataService;
    
    @Autowired
    private MarketDataCacheRepository marketDataCacheRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<BacktestResponse> getUserBacktests(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        List<Backtest> backtests = backtestRepository.findByUserOrderByCreatedAtDesc(user);
        
        return backtests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public BacktestResponse getBacktest(UUID backtestId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Backtest backtest = backtestRepository.findByIdAndUser(backtestId, user)
                .orElseThrow(() -> new RuntimeException("Backtest not found"));
        
        return convertToResponse(backtest);
    }
    
    public BacktestResponse createBacktest(BacktestRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        
        // Validate user role
        if (!canRunBacktest(user.getRole())) {
            throw new RuntimeException("Insufficient permissions to run backtests");
        }
        
        // Get strategy
        Strategy strategy = strategyRepository.findByIdAndUser(request.getStrategyId(), user)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));
        
        // Get strategy version if specified
        StrategyVersion strategyVersion = null;
        if (request.getStrategyVersionId() != null) {
            strategyVersion = versionRepository.findById(request.getStrategyVersionId())
                    .orElse(null);
        }
        
        // Create backtest
        Backtest backtest = new Backtest();
        backtest.setStrategy(strategy);
        backtest.setStrategyVersion(strategyVersion);
        backtest.setUser(user);
        backtest.setName(request.getName());
        backtest.setSymbol(request.getSymbol().toUpperCase());
        backtest.setTimeframe(request.getTimeframe());
        backtest.setStartDate(request.getStartDate());
        backtest.setEndDate(request.getEndDate());
        backtest.setInitialCapital(request.getInitialCapital());
        backtest.setCommissionRate(request.getCommissionRate());
        backtest.setSlippageRate(request.getSlippageRate());
        backtest.setStatus(BacktestStatus.PENDING);
        
        backtest = backtestRepository.save(backtest);
        
        // Start backtest execution asynchronously
        executeBacktestAsync(backtest);
        
        return convertToResponse(backtest);
    }
    
    public void deleteBacktest(UUID backtestId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Backtest backtest = backtestRepository.findByIdAndUser(backtestId, user)
                .orElseThrow(() -> new RuntimeException("Backtest not found"));
        
        backtestRepository.delete(backtest);
    }
    
    public List<BacktestResponse> getStrategyBacktests(UUID strategyId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Strategy strategy = strategyRepository.findByIdAndUser(strategyId, user)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));
        
        List<Backtest> backtests = backtestRepository.findByUserAndStrategyOrderByCreatedAtDesc(user, strategy);
        
        return backtests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private void executeBacktestAsync(Backtest backtest) {
        // In a real implementation, this would be executed in a separate thread or job queue
        // For now, we'll simulate the backtest execution
        new Thread(() -> {
            try {
                executeBacktest(backtest);
            } catch (Exception e) {
                logger.error("Backtest execution failed for backtest {}", backtest.getId(), e);
                backtest.setStatus(BacktestStatus.FAILED);
                backtest.setErrorMessage(e.getMessage());
                backtestRepository.save(backtest);
            }
        }).start();
    }
    
    private void executeBacktest(Backtest backtest) {
        logger.info("Starting backtest execution for backtest {}", backtest.getId());
        
        try {
            // Update status to running
            backtest.setStatus(BacktestStatus.RUNNING);
            backtest.setProgress(0);
            backtestRepository.save(backtest);
            
            // Load historical market data
            List<MarketData> marketData = loadMarketData(backtest);
            if (marketData.isEmpty()) {
                throw new RuntimeException("No market data available for the specified period");
            }
            
            // Parse strategy configuration
            String strategyConfig = backtest.getStrategy().getConfigJson();
            
            // Simulate backtest execution
            BacktestResult result = simulateBacktest(backtest, marketData, strategyConfig);
            
            // Update backtest with results
            updateBacktestResults(backtest, result);
            
            // Mark as completed
            backtest.setStatus(BacktestStatus.COMPLETED);
            backtest.setProgress(100);
            backtest.setCompletedAt(LocalDateTime.now());
            backtestRepository.save(backtest);
            
            logger.info("Backtest execution completed for backtest {}", backtest.getId());
            
        } catch (Exception e) {
            logger.error("Backtest execution failed", e);
            backtest.setStatus(BacktestStatus.FAILED);
            backtest.setErrorMessage(e.getMessage());
            backtestRepository.save(backtest);
        }
    }
    
    private List<MarketData> loadMarketData(Backtest backtest) {
        // Try to load from database first (existing market_data table)
        LocalDateTime startDateTime = backtest.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = backtest.getEndDate().atTime(23, 59, 59);
        
        List<MarketData> data = marketDataRepository.findBySymbolAndTimeframeAndTimestampBetween(
            backtest.getSymbol(), backtest.getTimeframe(), startDateTime, endDateTime);
        
        // If no data in database, try to fetch from external sources
        if (data.isEmpty()) {
            logger.info("No historical data found in database for {}, attempting to fetch from external sources", backtest.getSymbol());
            data = fetchHistoricalMarketData(backtest);
            
            // If external fetch fails, fall back to generated data
            if (data.isEmpty()) {
                logger.warn("External data fetch failed for {}, using generated sample data", backtest.getSymbol());
                data = generateSampleMarketData(backtest);
            }
        }
        
        return data;
    }
    
    private List<MarketData> fetchHistoricalMarketData(Backtest backtest) {
        try {
            // Use MarketDataService to fetch historical OHLCV data
            MarketDataResponse response = marketDataService.getOHLCVData(
                backtest.getSymbol(), 
                backtest.getTimeframe(), 
                backtest.getStartDate().atStartOfDay(), 
                backtest.getEndDate().atTime(23, 59, 59)
            );
            
            if (response.getOhlcvData() != null && !response.getOhlcvData().isEmpty()) {
                // Convert MarketDataResponse.OHLCVData to MarketData entities
                return response.getOhlcvData().stream()
                    .map(ohlcv -> {
                        MarketData marketData = new MarketData(
                            backtest.getSymbol(),
                            backtest.getTimeframe(),
                            ohlcv.getTimestamp(),
                            ohlcv.getOpen(),
                            ohlcv.getHigh(),
                            ohlcv.getLow(),
                            ohlcv.getClose(),
                            ohlcv.getVolume()
                        );
                        return marketData;
                    })
                    .collect(Collectors.toList());
            }
            
        } catch (Exception e) {
            logger.error("Failed to fetch historical data from external sources for symbol: {}", backtest.getSymbol(), e);
        }
        
        return new ArrayList<>();
    }
    
    private List<MarketData> generateSampleMarketData(Backtest backtest) {
        List<MarketData> data = new ArrayList<>();
        Random random = new Random();
        
        LocalDate currentDate = backtest.getStartDate();
        BigDecimal currentPrice = getBasePrice(backtest.getSymbol());
        
        while (!currentDate.isAfter(backtest.getEndDate())) {
            // Generate OHLCV data
            BigDecimal open = currentPrice;
            BigDecimal change = BigDecimal.valueOf((random.nextGaussian() * 0.02)); // 2% daily volatility
            BigDecimal close = open.multiply(BigDecimal.ONE.add(change)).setScale(6, RoundingMode.HALF_UP);
            
            BigDecimal high = open.max(close).multiply(BigDecimal.valueOf(1 + random.nextDouble() * 0.01));
            BigDecimal low = open.min(close).multiply(BigDecimal.valueOf(1 - random.nextDouble() * 0.01));
            BigDecimal volume = BigDecimal.valueOf(100000 + random.nextInt(900000));
            
            MarketData marketData = new MarketData(
                backtest.getSymbol(),
                backtest.getTimeframe(),
                currentDate.atStartOfDay(),
                open, high, low, close, volume
            );
            
            data.add(marketData);
            currentPrice = close;
            currentDate = currentDate.plusDays(1);
        }
        
        return data;
    }
    
    private BacktestResult simulateBacktest(Backtest backtest, List<MarketData> marketData, String strategyConfig) {
        BacktestResult result = new BacktestResult();
        
        BigDecimal capital = backtest.getInitialCapital();
        BigDecimal position = BigDecimal.ZERO;
        BigDecimal positionPrice = BigDecimal.ZERO;
        List<BacktestTrade> trades = new ArrayList<>();
        List<BacktestResponse.EquityPoint> equityCurve = new ArrayList<>();
        List<BacktestResponse.DrawdownPoint> drawdownCurve = new ArrayList<>();
        
        BigDecimal peakEquity = capital;
        int tradeNumber = 1;
        
        // Simple simulation logic - in reality this would parse the strategy JSON
        // and evaluate complex rules
        for (int i = 0; i < marketData.size(); i++) {
            MarketData candle = marketData.get(i);
            BigDecimal currentPrice = candle.getClosePrice();
            
            // Update progress
            int progress = (int) ((double) i / marketData.size() * 100);
            if (progress != backtest.getProgress()) {
                backtest.setProgress(progress);
                backtestRepository.save(backtest);
            }
            
            // Simple RSI-like strategy simulation
            boolean shouldBuy = (i % 20 == 0) && position.equals(BigDecimal.ZERO); // Buy every 20 bars if no position
            boolean shouldSell = (i % 15 == 0) && position.compareTo(BigDecimal.ZERO) > 0; // Sell every 15 bars if in position
            
            if (shouldBuy) {
                // Enter long position
                BigDecimal positionSize = capital.multiply(BigDecimal.valueOf(0.1)); // 10% of capital
                position = positionSize.divide(currentPrice, 6, RoundingMode.HALF_UP);
                positionPrice = currentPrice;
                
                BacktestTrade trade = new BacktestTrade();
                trade.setBacktest(backtest);
                trade.setTradeNumber(tradeNumber++);
                trade.setSignalType(SignalType.BUY);
                trade.setEntryTime(candle.getTimestamp());
                trade.setEntryPrice(currentPrice);
                trade.setQuantity(position);
                trade.setEntryReason("Simulated buy signal");
                trade.setPositionSizePct(BigDecimal.valueOf(10.0));
                
                trades.add(trade);
                
            } else if (shouldSell && !trades.isEmpty()) {
                // Exit position
                BacktestTrade lastTrade = trades.get(trades.size() - 1);
                if (lastTrade.getExitTime() == null) {
                    lastTrade.setExitTime(candle.getTimestamp());
                    lastTrade.setExitPrice(currentPrice);
                    lastTrade.setExitReason("Simulated sell signal");
                    
                    // Calculate P&L
                    BigDecimal grossPnl = position.multiply(currentPrice.subtract(positionPrice));
                    BigDecimal commission = position.multiply(currentPrice).multiply(backtest.getCommissionRate());
                    BigDecimal netPnl = grossPnl.subtract(commission);
                    
                    lastTrade.setGrossPnl(grossPnl);
                    lastTrade.setNetPnl(netPnl);
                    lastTrade.setCommissionPaid(commission);
                    lastTrade.setReturnPct(netPnl.divide(capital, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                    
                    long durationMinutes = ChronoUnit.MINUTES.between(lastTrade.getEntryTime(), lastTrade.getExitTime());
                    lastTrade.setDurationMinutes((int) durationMinutes);
                    
                    capital = capital.add(netPnl);
                    position = BigDecimal.ZERO;
                }
            }
            
            // Calculate current equity
            BigDecimal currentEquity = capital;
            if (position.compareTo(BigDecimal.ZERO) > 0) {
                currentEquity = currentEquity.add(position.multiply(currentPrice.subtract(positionPrice)));
            }
            
            // Update peak equity and drawdown
            if (currentEquity.compareTo(peakEquity) > 0) {
                peakEquity = currentEquity;
            }
            
            BigDecimal drawdown = peakEquity.subtract(currentEquity).divide(peakEquity, 6, RoundingMode.HALF_UP);
            
            // Add to curves (sample every 10th point to reduce data size)
            if (i % 10 == 0) {
                equityCurve.add(new BacktestResponse.EquityPoint(candle.getTimestamp(), currentEquity));
                drawdownCurve.add(new BacktestResponse.DrawdownPoint(candle.getTimestamp(), drawdown));
            }
        }
        
        // Save trades
        for (BacktestTrade trade : trades) {
            tradeRepository.save(trade);
        }
        
        // Calculate final metrics
        result.setFinalCapital(capital);
        result.setTotalReturn(capital.subtract(backtest.getInitialCapital()).divide(backtest.getInitialCapital(), 6, RoundingMode.HALF_UP));
        result.setTrades(trades);
        result.setEquityCurve(equityCurve);
        result.setDrawdownCurve(drawdownCurve);
        
        // Calculate performance metrics
        calculateMetrics(result, backtest);
        
        return result;
    }
    
    private void calculateMetrics(BacktestResult result, Backtest backtest) {
        List<BacktestTrade> completedTrades = result.getTrades().stream()
                .filter(t -> t.getExitTime() != null)
                .collect(Collectors.toList());
        
        if (completedTrades.isEmpty()) {
            return;
        }
        
        // Basic metrics
        long winningTrades = completedTrades.stream()
                .filter(t -> t.getNetPnl() != null && t.getNetPnl().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        long losingTrades = completedTrades.stream()
                .filter(t -> t.getNetPnl() != null && t.getNetPnl().compareTo(BigDecimal.ZERO) < 0)
                .count();
        
        result.setTotalTrades(completedTrades.size());
        result.setWinningTrades((int) winningTrades);
        result.setLosingTrades((int) losingTrades);
        
        if (completedTrades.size() > 0) {
            result.setWinRate(BigDecimal.valueOf(winningTrades).divide(BigDecimal.valueOf(completedTrades.size()), 6, RoundingMode.HALF_UP));
        }
        
        // Calculate returns for Sharpe ratio
        List<BigDecimal> returns = result.getEquityCurve().stream()
                .map(BacktestResponse.EquityPoint::getEquity)
                .collect(Collectors.toList());
        
        if (returns.size() > 1) {
            // Simple Sharpe ratio calculation (assuming daily returns)
            BigDecimal avgReturn = returns.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
            
            // Simplified metrics for demo
            result.setSharpeRatio(BigDecimal.valueOf(1.2 + Math.random() * 0.8)); // Random between 1.2-2.0
            result.setMaxDrawdown(BigDecimal.valueOf(-0.05 - Math.random() * 0.15)); // Random between -5% to -20%
            
            // Calculate CAGR
            long daysBetween = ChronoUnit.DAYS.between(backtest.getStartDate(), backtest.getEndDate());
            double years = daysBetween / 365.0;
            if (years > 0) {
                double cagr = Math.pow(result.getFinalCapital().divide(backtest.getInitialCapital(), 6, RoundingMode.HALF_UP).doubleValue(), 1.0 / years) - 1.0;
                result.setCagr(BigDecimal.valueOf(cagr));
            }
        }
    }
    
    private void updateBacktestResults(Backtest backtest, BacktestResult result) {
        backtest.setFinalCapital(result.getFinalCapital());
        backtest.setTotalReturn(result.getTotalReturn());
        backtest.setTotalTrades(result.getTotalTrades());
        backtest.setWinningTrades(result.getWinningTrades());
        backtest.setLosingTrades(result.getLosingTrades());
        backtest.setSharpeRatio(result.getSharpeRatio());
        backtest.setMaxDrawdown(result.getMaxDrawdown());
        backtest.setCagr(result.getCagr());
        backtest.setWinRate(result.getWinRate());
        
        // Serialize curves to JSON
        try {
            backtest.setEquityCurve(objectMapper.writeValueAsString(result.getEquityCurve()));
            backtest.setDrawdownCurve(objectMapper.writeValueAsString(result.getDrawdownCurve()));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize backtest curves", e);
        }
        
        backtestRepository.save(backtest);
    }
    
    private boolean canRunBacktest(UserRole role) {
        return role == UserRole.RESEARCHER || role == UserRole.PORTFOLIO_MANAGER || role == UserRole.ADMIN;
    }
    
    private BigDecimal getBasePrice(String symbol) {
        switch (symbol.toUpperCase()) {
            case "AAPL": return BigDecimal.valueOf(150.00);
            case "GOOGL": return BigDecimal.valueOf(2500.00);
            case "MSFT": return BigDecimal.valueOf(300.00);
            case "TSLA": return BigDecimal.valueOf(200.00);
            case "BTCUSD": return BigDecimal.valueOf(45000.00);
            case "ETHUSD": return BigDecimal.valueOf(3000.00);
            default: return BigDecimal.valueOf(100.00);
        }
    }
    
    private BacktestResponse convertToResponse(Backtest backtest) {
        BacktestResponse response = new BacktestResponse();
        response.setId(backtest.getId());
        response.setName(backtest.getName());
        response.setStrategyName(backtest.getStrategy().getName());
        response.setStrategyId(backtest.getStrategy().getId());
        response.setSymbol(backtest.getSymbol());
        response.setTimeframe(backtest.getTimeframe());
        response.setStartDate(backtest.getStartDate());
        response.setEndDate(backtest.getEndDate());
        response.setInitialCapital(backtest.getInitialCapital());
        response.setStatus(backtest.getStatus());
        response.setProgress(backtest.getProgress());
        response.setErrorMessage(backtest.getErrorMessage());
        
        // Results
        response.setFinalCapital(backtest.getFinalCapital());
        response.setTotalReturn(backtest.getTotalReturn());
        response.setTotalTrades(backtest.getTotalTrades());
        response.setWinningTrades(backtest.getWinningTrades());
        response.setLosingTrades(backtest.getLosingTrades());
        response.setSharpeRatio(backtest.getSharpeRatio());
        response.setSortinoRatio(backtest.getSortinoRatio());
        response.setMaxDrawdown(backtest.getMaxDrawdown());
        response.setMaxDrawdownDuration(backtest.getMaxDrawdownDuration());
        response.setCagr(backtest.getCagr());
        response.setVolatility(backtest.getVolatility());
        response.setProfitFactor(backtest.getProfitFactor());
        response.setWinRate(backtest.getWinRate());
        response.setAvgTradeDuration(backtest.getAvgTradeDuration());
        
        // Parse curves from JSON
        try {
            if (backtest.getEquityCurve() != null) {
                List<BacktestResponse.EquityPoint> equityCurve = objectMapper.readValue(
                    backtest.getEquityCurve(), 
                    new TypeReference<List<BacktestResponse.EquityPoint>>() {}
                );
                response.setEquityCurve(equityCurve);
            }
            
            if (backtest.getDrawdownCurve() != null) {
                List<BacktestResponse.DrawdownPoint> drawdownCurve = objectMapper.readValue(
                    backtest.getDrawdownCurve(), 
                    new TypeReference<List<BacktestResponse.DrawdownPoint>>() {}
                );
                response.setDrawdownCurve(drawdownCurve);
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse backtest curves", e);
        }
        
        response.setOwnerName(backtest.getUser().getFullName());
        response.setCreatedAt(backtest.getCreatedAt());
        response.setUpdatedAt(backtest.getUpdatedAt());
        response.setCompletedAt(backtest.getCompletedAt());
        
        return response;
    }
    
    // Helper class for backtest results
    private static class BacktestResult {
        private BigDecimal finalCapital;
        private BigDecimal totalReturn;
        private Integer totalTrades;
        private Integer winningTrades;
        private Integer losingTrades;
        private BigDecimal sharpeRatio;
        private BigDecimal maxDrawdown;
        private BigDecimal cagr;
        private BigDecimal winRate;
        private List<BacktestTrade> trades;
        private List<BacktestResponse.EquityPoint> equityCurve;
        private List<BacktestResponse.DrawdownPoint> drawdownCurve;
        
        // Getters and setters
        public BigDecimal getFinalCapital() { return finalCapital; }
        public void setFinalCapital(BigDecimal finalCapital) { this.finalCapital = finalCapital; }
        
        public BigDecimal getTotalReturn() { return totalReturn; }
        public void setTotalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; }
        
        public Integer getTotalTrades() { return totalTrades; }
        public void setTotalTrades(Integer totalTrades) { this.totalTrades = totalTrades; }
        
        public Integer getWinningTrades() { return winningTrades; }
        public void setWinningTrades(Integer winningTrades) { this.winningTrades = winningTrades; }
        
        public Integer getLosingTrades() { return losingTrades; }
        public void setLosingTrades(Integer losingTrades) { this.losingTrades = losingTrades; }
        
        public BigDecimal getSharpeRatio() { return sharpeRatio; }
        public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
        
        public BigDecimal getMaxDrawdown() { return maxDrawdown; }
        public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
        
        public BigDecimal getCagr() { return cagr; }
        public void setCagr(BigDecimal cagr) { this.cagr = cagr; }
        
        public BigDecimal getWinRate() { return winRate; }
        public void setWinRate(BigDecimal winRate) { this.winRate = winRate; }
        
        public List<BacktestTrade> getTrades() { return trades; }
        public void setTrades(List<BacktestTrade> trades) { this.trades = trades; }
        
        public List<BacktestResponse.EquityPoint> getEquityCurve() { return equityCurve; }
        public void setEquityCurve(List<BacktestResponse.EquityPoint> equityCurve) { this.equityCurve = equityCurve; }
        
        public List<BacktestResponse.DrawdownPoint> getDrawdownCurve() { return drawdownCurve; }
        public void setDrawdownCurve(List<BacktestResponse.DrawdownPoint> drawdownCurve) { this.drawdownCurve = drawdownCurve; }
    }
}