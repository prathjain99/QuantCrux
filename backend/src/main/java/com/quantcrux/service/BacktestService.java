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
            
            // Load historical market data using the new method
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
        
        // Parse strategy configuration for real rule evaluation
        StrategyConfig config = parseStrategyConfig(strategyConfig);
        TechnicalIndicators indicators = new TechnicalIndicators();
        
        for (int i = 0; i < marketData.size(); i++) {
            MarketData candle = marketData.get(i);
            BigDecimal openPrice = candle.getOpenPrice();
            BigDecimal highPrice = candle.getHighPrice();
            BigDecimal lowPrice = candle.getLowPrice();
            BigDecimal closePrice = candle.getClosePrice();
            BigDecimal volume = candle.getVolume();
            
            // Update progress
            int progress = (int) ((double) i / marketData.size() * 100);
            if (progress != backtest.getProgress()) {
                backtest.setProgress(progress);
                backtestRepository.save(backtest);
            }
            
            // Update technical indicators with current candle
            indicators.update(openPrice, highPrice, lowPrice, closePrice, volume);
            
            // Evaluate strategy rules for entry signals
            boolean shouldBuy = false;
            boolean shouldSell = false;
            
            if (position.equals(BigDecimal.ZERO) && i >= config.getMinimumBars()) {
                shouldBuy = evaluateEntryRules(config, indicators, closePrice);
            }
            
            if (position.compareTo(BigDecimal.ZERO) > 0) {
                shouldSell = evaluateExitRules(config, indicators, closePrice, positionPrice);
            }
            
            if (shouldBuy) {
                // Calculate position size based on strategy config
                BigDecimal positionSizePct = config.getPositionSizePct();
                BigDecimal positionSize = capital.multiply(positionSizePct.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
                
                // Apply slippage and commission
                BigDecimal executionPrice = applySlippage(closePrice, OrderSide.BUY, backtest.getSlippageRate());
                position = positionSize.divide(executionPrice, 6, RoundingMode.HALF_UP);
                positionPrice = executionPrice;
                
                // Deduct commission from capital
                BigDecimal commission = positionSize.multiply(backtest.getCommissionRate());
                capital = capital.subtract(commission);
                
                BacktestTrade trade = new BacktestTrade();
                trade.setBacktest(backtest);
                trade.setTradeNumber(tradeNumber++);
                trade.setSignalType(SignalType.BUY);
                trade.setEntryTime(candle.getTimestamp());
                trade.setEntryPrice(executionPrice);
                trade.setQuantity(position);
                trade.setEntryReason(getEntryReason(config, indicators));
                trade.setPositionSizePct(positionSizePct);
                trade.setCommissionPaid(commission);
                trade.setEntryIndicators(serializeIndicatorValues(indicators));
                
                trades.add(trade);
                
            } else if (shouldSell && !trades.isEmpty()) {
                // Exit position
                BacktestTrade lastTrade = trades.get(trades.size() - 1);
                if (lastTrade.getExitTime() == null) {
                    // Apply slippage for exit
                    BigDecimal executionPrice = applySlippage(closePrice, OrderSide.SELL, backtest.getSlippageRate());
                    
                    lastTrade.setExitTime(candle.getTimestamp());
                    lastTrade.setExitPrice(executionPrice);
                    lastTrade.setExitReason(getExitReason(config, indicators));
                    lastTrade.setExitIndicators(serializeIndicatorValues(indicators));
                    
                    // Calculate P&L
                    BigDecimal grossPnl = position.multiply(executionPrice.subtract(positionPrice));
                    BigDecimal commission = position.multiply(executionPrice).multiply(backtest.getCommissionRate());
                    BigDecimal netPnl = grossPnl.subtract(commission);
                    
                    lastTrade.setGrossPnl(grossPnl);
                    lastTrade.setNetPnl(netPnl);
                    lastTrade.setCommissionPaid(commission);
                    
                    // Calculate return percentage based on position cost
                    BigDecimal positionCost = position.multiply(positionPrice);
                    if (positionCost.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal returnPct = netPnl.divide(positionCost, 6, RoundingMode.HALF_UP);
                        lastTrade.setReturnPct(returnPct);
                    }
                    
                    long durationMinutes = ChronoUnit.MINUTES.between(lastTrade.getEntryTime(), lastTrade.getExitTime());
                    lastTrade.setDurationMinutes((int) durationMinutes);
                    
                    capital = capital.add(netPnl);
                    position = BigDecimal.ZERO;
                }
            }
            
            // Calculate current equity
            BigDecimal currentEquity = capital;
            if (position.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal unrealizedPnl = position.multiply(closePrice.subtract(positionPrice));
                currentEquity = currentEquity.add(unrealizedPnl);
            }
            
            // Update peak equity and drawdown
            if (currentEquity.compareTo(peakEquity) > 0) {
                peakEquity = currentEquity;
            }
            
            BigDecimal drawdown = peakEquity.subtract(currentEquity).divide(peakEquity, 6, RoundingMode.HALF_UP);
            
            // Add to curves (sample based on timeframe to reduce data size)
            int sampleRate = getSampleRate(backtest.getTimeframe());
            if (i % sampleRate == 0) {
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
        
        // Calculate trade statistics
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
        
        // Calculate profit factor
        BigDecimal totalWins = completedTrades.stream()
                .filter(t -> t.getNetPnl() != null && t.getNetPnl().compareTo(BigDecimal.ZERO) > 0)
                .map(BacktestTrade::getNetPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalLosses = completedTrades.stream()
                .filter(t -> t.getNetPnl() != null && t.getNetPnl().compareTo(BigDecimal.ZERO) < 0)
                .map(t -> t.getNetPnl().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalLosses.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profitFactor = totalWins.divide(totalLosses, 6, RoundingMode.HALF_UP);
            result.setProfitFactor(profitFactor);
        }
        
        // Calculate daily returns from equity curve
        List<BigDecimal> dailyReturns = new ArrayList<>();
        List<BacktestResponse.EquityPoint> equityCurve = result.getEquityCurve();
        
        for (int i = 1; i < equityCurve.size(); i++) {
            BigDecimal prevEquity = equityCurve.get(i - 1).getEquity();
            BigDecimal currentEquity = equityCurve.get(i).getEquity();
            
            if (prevEquity.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal dailyReturn = currentEquity.subtract(prevEquity).divide(prevEquity, 6, RoundingMode.HALF_UP);
                dailyReturns.add(dailyReturn);
            }
        }
        
        if (dailyReturns.size() > 1) {
            // Calculate volatility
            BigDecimal avgReturn = dailyReturns.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(dailyReturns.size()), 6, RoundingMode.HALF_UP);
            
            BigDecimal variance = dailyReturns.stream()
                    .map(r -> r.subtract(avgReturn).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(dailyReturns.size()), 6, RoundingMode.HALF_UP);
            
            BigDecimal volatility = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
            result.setVolatility(volatility);
            
            // Calculate Sharpe ratio (assuming 5% annual risk-free rate)
            BigDecimal riskFreeRate = BigDecimal.valueOf(0.05).divide(BigDecimal.valueOf(252), 6, RoundingMode.HALF_UP);
            if (volatility.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal sharpeRatio = avgReturn.subtract(riskFreeRate).divide(volatility, 6, RoundingMode.HALF_UP);
                result.setSharpeRatio(sharpeRatio);
            }
            
            // Calculate Sortino ratio (downside deviation)
            List<BigDecimal> negativeReturns = dailyReturns.stream()
                    .filter(r -> r.compareTo(BigDecimal.ZERO) < 0)
                    .collect(Collectors.toList());
            
            if (!negativeReturns.isEmpty()) {
                BigDecimal downsideVariance = negativeReturns.stream()
                        .map(r -> r.pow(2))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(negativeReturns.size()), 6, RoundingMode.HALF_UP);
                
                BigDecimal downsideDeviation = BigDecimal.valueOf(Math.sqrt(downsideVariance.doubleValue()));
                if (downsideDeviation.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal sortinoRatio = avgReturn.subtract(riskFreeRate).divide(downsideDeviation, 6, RoundingMode.HALF_UP);
                    result.setSortinoRatio(sortinoRatio);
                }
            }
        }
        
        // Calculate maximum drawdown and duration
        calculateMaxDrawdownMetrics(result);
        
        // Calculate CAGR
        long daysBetween = ChronoUnit.DAYS.between(backtest.getStartDate(), backtest.getEndDate());
        if (daysBetween > 0) {
            double years = daysBetween / 365.0;
            double cagr = Math.pow(result.getFinalCapital().divide(backtest.getInitialCapital(), 6, RoundingMode.HALF_UP).doubleValue(), 1.0 / years) - 1.0;
            result.setCagr(BigDecimal.valueOf(cagr));
        }
        
        // Calculate average trade duration
        if (!completedTrades.isEmpty()) {
            double avgDuration = completedTrades.stream()
                    .filter(t -> t.getDurationMinutes() != null)
                    .mapToInt(BacktestTrade::getDurationMinutes)
                    .average()
                    .orElse(0.0);
            result.setAvgTradeDuration((int) avgDuration);
        }
    }
    
    private void calculateMaxDrawdownMetrics(BacktestResult result) {
        List<BacktestResponse.EquityPoint> equityCurve = result.getEquityCurve();
        if (equityCurve.size() < 2) {
            return;
        }
        
        BigDecimal peak = equityCurve.get(0).getEquity();
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        int maxDrawdownDuration = 0;
        int currentDrawdownDuration = 0;
        LocalDateTime drawdownStart = null;
        
        for (BacktestResponse.EquityPoint point : equityCurve) {
            BigDecimal currentEquity = point.getEquity();
            
            if (currentEquity.compareTo(peak) > 0) {
                peak = currentEquity;
                currentDrawdownDuration = 0;
                drawdownStart = null;
            } else {
                if (drawdownStart == null) {
                    drawdownStart = point.getTimestamp();
                }
                currentDrawdownDuration++;
                
                BigDecimal drawdown = peak.subtract(currentEquity).divide(peak, 6, RoundingMode.HALF_UP);
                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                    maxDrawdownDuration = currentDrawdownDuration;
                }
            }
        }
        
        result.setMaxDrawdown(maxDrawdown);
        result.setMaxDrawdownDuration(maxDrawdownDuration);
    }
    
    private StrategyConfig parseStrategyConfig(String configJson) {
        try {
            JsonNode config = objectMapper.readTree(configJson);
            StrategyConfig strategyConfig = new StrategyConfig();
            
            // Parse position sizing
            JsonNode position = config.get("position");
            if (position != null) {
                strategyConfig.setPositionSizePct(BigDecimal.valueOf(position.path("capital_pct").asDouble(25.0)));
                strategyConfig.setLeverage(BigDecimal.valueOf(position.path("leverage").asDouble(1.0)));
            }
            
            // Parse indicators
            JsonNode indicators = config.get("indicators");
            if (indicators != null && indicators.isArray()) {
                for (JsonNode indicator : indicators) {
                    String type = indicator.get("type").asText();
                    int period = indicator.path("period").asInt(14);
                    strategyConfig.addIndicator(type, period);
                }
            }
            
            // Parse entry rules
            JsonNode entry = config.get("entry");
            if (entry != null) {
                strategyConfig.setEntryLogic(entry.path("logic").asText("AND"));
                strategyConfig.setEntryRules(entry.get("rules"));
            }
            
            // Parse exit rules
            JsonNode exit = config.get("exit");
            if (exit != null) {
                strategyConfig.setExitLogic(exit.path("logic").asText("OR"));
                strategyConfig.setExitRules(exit.get("rules"));
            }
            
            return strategyConfig;
            
        } catch (Exception e) {
            logger.error("Failed to parse strategy config, using defaults", e);
            return getDefaultStrategyConfig();
        }
    }
    
    private boolean evaluateEntryRules(StrategyConfig config, TechnicalIndicators indicators, BigDecimal currentPrice) {
        return evaluateRules(config.getEntryRules(), config.getEntryLogic(), indicators, currentPrice);
    }
    
    private boolean evaluateExitRules(StrategyConfig config, TechnicalIndicators indicators, BigDecimal currentPrice, BigDecimal entryPrice) {
        // Check stop loss first
        if (config.getStopLossPct() != null) {
            BigDecimal stopLossPrice = entryPrice.multiply(BigDecimal.ONE.subtract(config.getStopLossPct().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)));
            if (currentPrice.compareTo(stopLossPrice) <= 0) {
                return true;
            }
        }
        
        // Check take profit
        if (config.getTakeProfitPct() != null) {
            BigDecimal takeProfitPrice = entryPrice.multiply(BigDecimal.ONE.add(config.getTakeProfitPct().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)));
            if (currentPrice.compareTo(takeProfitPrice) >= 0) {
                return true;
            }
        }
        
        return evaluateRules(config.getExitRules(), config.getExitLogic(), indicators, currentPrice);
    }
    
    private boolean evaluateRules(JsonNode rules, String logic, TechnicalIndicators indicators, BigDecimal currentPrice) {
        if (rules == null || !rules.isArray()) {
            return false;
        }
        
        boolean result = "AND".equals(logic);
        
        for (JsonNode rule : rules) {
            boolean ruleResult = evaluateRule(rule, indicators, currentPrice);
            
            if ("AND".equals(logic)) {
                result = result && ruleResult;
                if (!result) break; // Short circuit
            } else if ("OR".equals(logic)) {
                result = result || ruleResult;
                if (result) break; // Short circuit
            }
        }
        
        return result;
    }
    
    private boolean evaluateRule(JsonNode rule, TechnicalIndicators indicators, BigDecimal currentPrice) {
        try {
            String indicator = rule.get("indicator").asText();
            String operator = rule.get("operator").asText();
            
            BigDecimal indicatorValue = getIndicatorValue(indicator, indicators, currentPrice);
            if (indicatorValue == null) {
                return false;
            }
            
            if (rule.has("value")) {
                BigDecimal targetValue = BigDecimal.valueOf(rule.get("value").asDouble());
                return compareValues(indicatorValue, operator, targetValue);
            } else if (rule.has("compare_to")) {
                String compareToIndicator = rule.get("compare_to").asText();
                BigDecimal compareValue = getIndicatorValue(compareToIndicator, indicators, currentPrice);
                if (compareValue != null) {
                    return compareValues(indicatorValue, operator, compareValue);
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to evaluate rule", e);
        }
        
        return false;
    }
    
    private BigDecimal getIndicatorValue(String indicator, TechnicalIndicators indicators, BigDecimal currentPrice) {
        switch (indicator.toUpperCase()) {
            case "PRICE":
                return currentPrice;
            case "RSI":
                return indicators.getRsi();
            case "SMA_50":
                return indicators.getSma50();
            case "SMA_20":
                return indicators.getSma20();
            case "EMA_20":
                return indicators.getEma20();
            case "MACD":
                return indicators.getMacd();
            case "MACD_SIGNAL":
                return indicators.getMacdSignal();
            default:
                logger.warn("Unknown indicator: {}", indicator);
                return null;
        }
    }
    
    private boolean compareValues(BigDecimal value1, String operator, BigDecimal value2) {
        switch (operator) {
            case ">": return value1.compareTo(value2) > 0;
            case "<": return value1.compareTo(value2) < 0;
            case ">=": return value1.compareTo(value2) >= 0;
            case "<=": return value1.compareTo(value2) <= 0;
            case "=":
            case "==": return value1.compareTo(value2) == 0;
            default: return false;
        }
    }
    
    private BigDecimal applySlippage(BigDecimal price, OrderSide side, BigDecimal slippageRate) {
        BigDecimal slippage = price.multiply(slippageRate);
        return side == OrderSide.BUY ? price.add(slippage) : price.subtract(slippage);
    }
    
    private String getEntryReason(StrategyConfig config, TechnicalIndicators indicators) {
        StringBuilder reason = new StringBuilder("Entry: ");
        if (indicators.getRsi() != null) {
            reason.append("RSI=").append(indicators.getRsi().setScale(2, RoundingMode.HALF_UP)).append(" ");
        }
        if (indicators.getSma50() != null) {
            reason.append("SMA50=").append(indicators.getSma50().setScale(2, RoundingMode.HALF_UP));
        }
        return reason.toString();
    }
    
    private String getExitReason(StrategyConfig config, TechnicalIndicators indicators) {
        StringBuilder reason = new StringBuilder("Exit: ");
        if (indicators.getRsi() != null) {
            reason.append("RSI=").append(indicators.getRsi().setScale(2, RoundingMode.HALF_UP)).append(" ");
        }
        return reason.toString();
    }
    
    private String serializeIndicatorValues(TechnicalIndicators indicators) {
        try {
            Map<String, Object> values = new HashMap<>();
            if (indicators.getRsi() != null) values.put("RSI", indicators.getRsi());
            if (indicators.getSma50() != null) values.put("SMA_50", indicators.getSma50());
            if (indicators.getSma20() != null) values.put("SMA_20", indicators.getSma20());
            if (indicators.getEma20() != null) values.put("EMA_20", indicators.getEma20());
            if (indicators.getMacd() != null) values.put("MACD", indicators.getMacd());
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize indicator values", e);
            return "{}";
        }
    }
    
    private int getSampleRate(String timeframe) {
        switch (timeframe.toLowerCase()) {
            case "1m": return 60; // Sample every hour
            case "5m": return 12; // Sample every hour
            case "15m": return 4; // Sample every hour
            case "30m": return 2; // Sample every hour
            case "1h": return 1; // Sample every hour
            case "4h": return 1; // Sample every 4 hours
            case "1d": return 1; // Sample every day
            default: return 10;
        }
    }
    
    private StrategyConfig getDefaultStrategyConfig() {
        StrategyConfig config = new StrategyConfig();
        config.setPositionSizePct(BigDecimal.valueOf(25.0));
        config.setLeverage(BigDecimal.valueOf(1.0));
        config.setEntryLogic("AND");
        config.setExitLogic("OR");
        return config;
    }
    
    // Helper classes for strategy configuration and indicators
    private static class StrategyConfig {
        private BigDecimal positionSizePct = BigDecimal.valueOf(25.0);
        private BigDecimal leverage = BigDecimal.valueOf(1.0);
        private String entryLogic = "AND";
        private String exitLogic = "OR";
        private JsonNode entryRules;
        private JsonNode exitRules;
        private Map<String, Integer> indicators = new HashMap<>();
        private BigDecimal stopLossPct;
        private BigDecimal takeProfitPct;
        
        public void addIndicator(String type, int period) {
            indicators.put(type + "_" + period, period);
        }
        
        public int getMinimumBars() {
            return indicators.values().stream().mapToInt(Integer::intValue).max().orElse(50);
        }
        
                .collect(Collectors.toList());
        
        public BigDecimal getPositionSizePct() { return positionSizePct; }
        public void setPositionSizePct(BigDecimal positionSizePct) { this.positionSizePct = positionSizePct; }
        
        public BigDecimal getLeverage() { return leverage; }
        public void setLeverage(BigDecimal leverage) { this.leverage = leverage; }
        
        public String getEntryLogic() { return entryLogic; }
        public void setEntryLogic(String entryLogic) { this.entryLogic = entryLogic; }
        
        public String getExitLogic() { return exitLogic; }
        public void setExitLogic(String exitLogic) { this.exitLogic = exitLogic; }
        
        public JsonNode getEntryRules() { return entryRules; }
        public void setEntryRules(JsonNode entryRules) { this.entryRules = entryRules; }
        
        public JsonNode getExitRules() { return exitRules; }
        public void setExitRules(JsonNode exitRules) { this.exitRules = exitRules; }
        
        public BigDecimal getStopLossPct() { return stopLossPct; }
        public void setStopLossPct(BigDecimal stopLossPct) { this.stopLossPct = stopLossPct; }
        
        public BigDecimal getTakeProfitPct() { return takeProfitPct; }
        public void setTakeProfitPct(BigDecimal takeProfitPct) { this.takeProfitPct = takeProfitPct; }
    }
    
    private static class TechnicalIndicators {
        private List<BigDecimal> prices = new ArrayList<>();
        private List<BigDecimal> volumes = new ArrayList<>();
        private BigDecimal rsi;
        private BigDecimal sma20;
        private BigDecimal sma50;
        private BigDecimal ema20;
        private BigDecimal macd;
        private BigDecimal macdSignal;
        
        public void update(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal volume) {
            prices.add(close);
            volumes.add(volume);
            
            // Keep only last 200 bars for efficiency
            if (prices.size() > 200) {
                prices.remove(0);
                volumes.remove(0);
            }
            
            // Calculate indicators
            if (prices.size() >= 14) {
                rsi = calculateRSI(prices, 14);
            }
            if (prices.size() >= 20) {
                sma20 = calculateSMA(prices, 20);
                ema20 = calculateEMA(prices, 20);
            }
            if (prices.size() >= 50) {
                sma50 = calculateSMA(prices, 50);
            }
            if (prices.size() >= 26) {
                Map<String, BigDecimal> macdValues = calculateMACD(prices);
                macd = macdValues.get("MACD");
                macdSignal = macdValues.get("MACD_Signal");
            }
        }
        
        private BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
            if (prices.size() < period + 1) {
                return BigDecimal.valueOf(50);
            }
            
            BigDecimal avgGain = BigDecimal.ZERO;
            BigDecimal avgLoss = BigDecimal.ZERO;
            
            for (int i = prices.size() - period; i < prices.size(); i++) {
                BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
                if (change.compareTo(BigDecimal.ZERO) > 0) {
                    avgGain = avgGain.add(change);
                } else {
                    avgLoss = avgLoss.add(change.abs());
                }
            }
            
            avgGain = avgGain.divide(BigDecimal.valueOf(period), 6, RoundingMode.HALF_UP);
            avgLoss = avgLoss.divide(BigDecimal.valueOf(period), 6, RoundingMode.HALF_UP);
            
            if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.valueOf(100);
            }
            
            BigDecimal rs = avgGain.divide(avgLoss, 6, RoundingMode.HALF_UP);
            return BigDecimal.valueOf(100).subtract(
                BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 6, RoundingMode.HALF_UP)
            );
        }
        
        private BigDecimal calculateSMA(List<BigDecimal> prices, int period) {
            if (prices.size() < period) {
                return prices.get(prices.size() - 1);
            }
            
            BigDecimal sum = BigDecimal.ZERO;
            for (int i = prices.size() - period; i < prices.size(); i++) {
                sum = sum.add(prices.get(i));
            }
            
            return sum.divide(BigDecimal.valueOf(period), 6, RoundingMode.HALF_UP);
        }
        
        private BigDecimal calculateEMA(List<BigDecimal> prices, int period) {
            if (prices.size() < period) {
                return prices.get(prices.size() - 1);
            }
            
            BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
            BigDecimal ema = calculateSMA(prices.subList(0, period), period);
            
            for (int i = period; i < prices.size(); i++) {
                ema = prices.get(i).multiply(multiplier).add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
            }
            
            return ema;
        }
        
        private Map<String, BigDecimal> calculateMACD(List<BigDecimal> prices) {
            Map<String, BigDecimal> result = new HashMap<>();
            
            if (prices.size() < 26) {
                result.put("MACD", BigDecimal.ZERO);
                result.put("MACD_Signal", BigDecimal.ZERO);
                return result;
            }
            
            BigDecimal ema12 = calculateEMA(prices, 12);
            BigDecimal ema26 = calculateEMA(prices, 26);
            BigDecimal macdLine = ema12.subtract(ema26);
            
            // Simple signal line (9-period EMA of MACD)
            BigDecimal signalLine = macdLine.multiply(BigDecimal.valueOf(0.9));
            
            result.put("MACD", macdLine);
            result.put("MACD_Signal", signalLine);
            
            return result;
        }
        
        // Getters
        public BigDecimal getRsi() { return rsi; }
        public BigDecimal getSma20() { return sma20; }
        public BigDecimal getSma50() { return sma50; }
        public BigDecimal getEma20() { return ema20; }
        public BigDecimal getMacd() { return macd; }
        public BigDecimal getMacdSignal() { return macdSignal; }
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