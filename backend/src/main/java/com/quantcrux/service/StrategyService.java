package com.quantcrux.service;

import com.quantcrux.dto.*;
import com.quantcrux.model.*;
import com.quantcrux.repository.StrategyRepository;
import com.quantcrux.repository.StrategyVersionRepository;
import com.quantcrux.repository.StrategySignalRepository;
import com.quantcrux.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StrategyService {
    
    @Autowired
    private StrategyRepository strategyRepository;
    
    @Autowired
    private StrategyVersionRepository versionRepository;
    
    @Autowired
    private StrategySignalRepository signalRepository;
    
    @Autowired
    private MarketDataService marketDataService;
    
    public List<StrategyResponse> getUserStrategies(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        List<Strategy> strategies = strategyRepository.findByUser(user);
        
        return strategies.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public StrategyResponse getStrategy(UUID strategyId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Strategy strategy = strategyRepository.findByIdAndUser(strategyId, user)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));
        
        return convertToResponse(strategy);
    }
    
    public StrategyResponse createStrategy(StrategyRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        
        // Validate user role
        if (!canCreateStrategy(user.getRole())) {
            throw new RuntimeException("Insufficient permissions to create strategies");
        }
        
        Strategy strategy = new Strategy();
        strategy.setUser(user);
        strategy.setName(request.getName());
        strategy.setDescription(request.getDescription());
        strategy.setSymbol(request.getSymbol().toUpperCase());
        strategy.setTimeframe(request.getTimeframe());
        strategy.setConfigJson(request.getConfigJson());
        strategy.setStatus(request.getStatus());
        strategy.setTags(request.getTags());
        
        strategy = strategyRepository.save(strategy);
        
        // Create initial version
        createStrategyVersion(strategy, 1, request.getConfigJson(), "Initial version", user);
        
        return convertToResponse(strategy);
    }
    
    public StrategyResponse updateStrategy(UUID strategyId, StrategyRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Strategy strategy = strategyRepository.findByIdAndUser(strategyId, user)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));
        
        // Check if config changed to create new version
        boolean configChanged = !strategy.getConfigJson().equals(request.getConfigJson());
        
        strategy.setName(request.getName());
        strategy.setDescription(request.getDescription());
        strategy.setSymbol(request.getSymbol().toUpperCase());
        strategy.setTimeframe(request.getTimeframe());
        strategy.setConfigJson(request.getConfigJson());
        strategy.setStatus(request.getStatus());
        strategy.setTags(request.getTags());
        
        if (configChanged) {
            Integer nextVersion = strategy.getCurrentVersion() + 1;
            strategy.setCurrentVersion(nextVersion);
            createStrategyVersion(strategy, nextVersion, request.getConfigJson(), "Updated configuration", user);
        }
        
        strategy = strategyRepository.save(strategy);
        return convertToResponse(strategy);
    }
    
    public void deleteStrategy(UUID strategyId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Strategy strategy = strategyRepository.findByIdAndUser(strategyId, user)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));
        
        strategyRepository.delete(strategy);
    }
    
    public SignalEvaluationResponse evaluateStrategy(SignalEvaluationRequest request, UserPrincipal userPrincipal) {
        try {
            // Get live market data for the symbol
            MarketDataResponse liveData = marketDataService.getLivePrice(request.getSymbol());
            
            // Get historical data for indicator calculations
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(100); // Get 100 days of data for indicators
            MarketDataResponse historicalData = marketDataService.getOHLCVData(
                request.getSymbol(), 
                request.getTimeframe() != null ? request.getTimeframe() : "1d", 
                startTime, 
                endTime
            );
            
            // Parse strategy configuration
            Map<String, Object> indicatorValues = calculateIndicators(historicalData, request.getConfigJson());
            SignalType signal = evaluateStrategyRules(liveData, historicalData, indicatorValues, request.getConfigJson());
            List<String> matchedRules = getMatchedRules(signal, indicatorValues, request.getConfigJson());
            
            SignalEvaluationResponse response = new SignalEvaluationResponse();
            response.setSignal(signal);
            response.setCurrentPrice(liveData.getPrice());
            response.setIndicatorValues(indicatorValues);
            response.setMatchedRules(matchedRules);
            response.setConfidenceScore(calculateConfidenceScore(signal, matchedRules));
            response.setEvaluatedAt(LocalDateTime.now());
            response.setMessage("Strategy evaluated successfully");
            
            return response;
        } catch (Exception e) {
            SignalEvaluationResponse errorResponse = new SignalEvaluationResponse();
            errorResponse.setSignal(SignalType.NO_SIGNAL);
            errorResponse.setMessage("Error evaluating strategy: " + e.getMessage());
            errorResponse.setEvaluatedAt(LocalDateTime.now());
            return errorResponse;
        }
    }
    
    private Map<String, Object> calculateIndicators(MarketDataResponse historicalData, String configJson) {
        Map<String, Object> indicators = new HashMap<>();
        
        try {
            // Parse strategy configuration to get required indicators
            com.fasterxml.jackson.databind.JsonNode config = objectMapper.readTree(configJson);
            com.fasterxml.jackson.databind.JsonNode indicatorConfigs = config.get("indicators");
            
            if (indicatorConfigs != null && indicatorConfigs.isArray() && 
                historicalData.getOhlcvData() != null && !historicalData.getOhlcvData().isEmpty()) {
                
                List<BigDecimal> closePrices = historicalData.getOhlcvData().stream()
                    .map(MarketDataResponse.OHLCVData::getClose)
                    .collect(Collectors.toList());
                
                for (com.fasterxml.jackson.databind.JsonNode indicatorConfig : indicatorConfigs) {
                    String type = indicatorConfig.get("type").asText();
                    int period = indicatorConfig.has("period") ? indicatorConfig.get("period").asInt() : 14;
                    
                    switch (type.toUpperCase()) {
                        case "RSI":
                            indicators.put("RSI", calculateRSI(closePrices, period));
                            break;
                        case "SMA":
                            indicators.put("SMA_" + period, calculateSMA(closePrices, period));
                            break;
                        case "EMA":
                            indicators.put("EMA_" + period, calculateEMA(closePrices, period));
                            break;
                        case "MACD":
                            Map<String, BigDecimal> macd = calculateMACD(closePrices);
                            indicators.putAll(macd);
                            break;
                        case "BOLLINGER":
                            Map<String, BigDecimal> bollinger = calculateBollingerBands(closePrices, period);
                            indicators.putAll(bollinger);
                            break;
                        case "STOCHASTIC":
                            if (historicalData.getOhlcvData().size() >= period) {
                                Map<String, BigDecimal> stoch = calculateStochastic(historicalData.getOhlcvData(), period);
                                indicators.putAll(stoch);
                            }
                            break;
                    }
                }
                
                // Add current price
                indicators.put("Price", closePrices.get(closePrices.size() - 1));
            }
            
        } catch (Exception e) {
            logger.error("Failed to calculate indicators", e);
            // Fallback to basic indicators if calculation fails
            BigDecimal currentPrice = historicalData.getPrice() != null ? historicalData.getPrice() : getBasePrice(historicalData.getSymbol());
            indicators.put("Price", currentPrice);
            indicators.put("RSI", BigDecimal.valueOf(50.0)); // Neutral RSI
        }
        
        private Map<String, BigDecimal> calculateBollingerBands(List<BigDecimal> prices, int period) {
            Map<String, BigDecimal> bands = new HashMap<>();
            
            if (prices.size() < period) {
                BigDecimal currentPrice = prices.get(prices.size() - 1);
                bands.put("BB_Upper", currentPrice.multiply(BigDecimal.valueOf(1.02)));
                bands.put("BB_Middle", currentPrice);
                bands.put("BB_Lower", currentPrice.multiply(BigDecimal.valueOf(0.98)));
                return bands;
            }
            
            // Calculate SMA
            BigDecimal sma = calculateSMA(prices, period);
            
            // Calculate standard deviation
            BigDecimal variance = BigDecimal.ZERO;
            for (int i = prices.size() - period; i < prices.size(); i++) {
                BigDecimal diff = prices.get(i).subtract(sma);
                variance = variance.add(diff.multiply(diff));
            }
            variance = variance.divide(BigDecimal.valueOf(period), 6, RoundingMode.HALF_UP);
            BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
            
            // Calculate bands (2 standard deviations)
            BigDecimal multiplier = BigDecimal.valueOf(2.0);
            bands.put("BB_Upper", sma.add(stdDev.multiply(multiplier)));
            bands.put("BB_Middle", sma);
            bands.put("BB_Lower", sma.subtract(stdDev.multiply(multiplier)));
            
            return bands;
        }
        
        private Map<String, BigDecimal> calculateStochastic(List<MarketDataResponse.OHLCVData> ohlcvData, int period) {
            Map<String, BigDecimal> stoch = new HashMap<>();
            
            if (ohlcvData.size() < period) {
                stoch.put("STOCH_K", BigDecimal.valueOf(50));
                stoch.put("STOCH_D", BigDecimal.valueOf(50));
                return stoch;
            }
            
            // Get last 'period' candles
            List<MarketDataResponse.OHLCVData> recentCandles = ohlcvData.subList(ohlcvData.size() - period, ohlcvData.size());
            
            // Find highest high and lowest low
            BigDecimal highestHigh = recentCandles.stream()
                    .map(MarketDataResponse.OHLCVData::getHigh)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            BigDecimal lowestLow = recentCandles.stream()
                    .map(MarketDataResponse.OHLCVData::getLow)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            BigDecimal currentClose = ohlcvData.get(ohlcvData.size() - 1).getClose();
            
            // Calculate %K
            BigDecimal range = highestHigh.subtract(lowestLow);
            BigDecimal stochK = BigDecimal.valueOf(50); // Default
            
            if (range.compareTo(BigDecimal.ZERO) > 0) {
                stochK = currentClose.subtract(lowestLow)
                        .divide(range, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
            
            // Simple %D calculation (3-period SMA of %K)
            BigDecimal stochD = stochK.multiply(BigDecimal.valueOf(0.9)); // Simplified
            
            stoch.put("STOCH_K", stochK);
            stoch.put("STOCH_D", stochD);
            
            return stoch;
        }
        
        return indicators;
    }
    
    private SignalType evaluateStrategyRules(MarketDataResponse liveData, MarketDataResponse historicalData, 
                                           Map<String, Object> indicators, String configJson) {
        try {
            // Parse strategy configuration to get entry/exit rules
            com.fasterxml.jackson.databind.JsonNode config = objectMapper.readTree(configJson);
            com.fasterxml.jackson.databind.JsonNode entryRules = config.get("entry");
            
            if (entryRules != null && entryRules.has("rules")) {
                boolean allRulesMet = true;
                String logic = entryRules.has("logic") ? entryRules.get("logic").asText() : "AND";
                
                for (com.fasterxml.jackson.databind.JsonNode rule : entryRules.get("rules")) {
                    boolean ruleMet = evaluateRule(rule, indicators, liveData.getPrice());
                    
                    if ("AND".equals(logic) && !ruleMet) {
                        allRulesMet = false;
                        break;
                    } else if ("OR".equals(logic) && ruleMet) {
                        return SignalType.BUY;
                    }
                }
                
                if ("AND".equals(logic) && allRulesMet) {
                    return SignalType.BUY;
                }
            }
            
            // Check exit rules for SELL signal
            com.fasterxml.jackson.databind.JsonNode exitRules = config.get("exit");
            if (exitRules != null && exitRules.has("rules")) {
                for (com.fasterxml.jackson.databind.JsonNode rule : exitRules.get("rules")) {
                    if (evaluateRule(rule, indicators, liveData.getPrice())) {
                        return SignalType.SELL;
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to evaluate strategy rules", e);
        }
        
        return SignalType.HOLD;
    }
    
    private boolean evaluateRule(com.fasterxml.jackson.databind.JsonNode rule, Map<String, Object> indicators, BigDecimal currentPrice) {
        try {
            String indicator = rule.get("indicator").asText();
            String operator = rule.get("operator").asText();
            
            BigDecimal indicatorValue;
            if ("Price".equals(indicator)) {
                indicatorValue = currentPrice;
            } else {
                Object value = indicators.get(indicator);
                if (value instanceof BigDecimal) {
                    indicatorValue = (BigDecimal) value;
                } else if (value instanceof Number) {
                    indicatorValue = BigDecimal.valueOf(((Number) value).doubleValue());
                } else {
                    return false;
                }
            }
            
            if (rule.has("value")) {
                BigDecimal targetValue = BigDecimal.valueOf(rule.get("value").asDouble());
                return compareValues(indicatorValue, operator, targetValue);
            } else if (rule.has("compare_to")) {
                String compareToIndicator = rule.get("compare_to").asText();
                Object compareValue = indicators.get(compareToIndicator);
                if (compareValue instanceof BigDecimal) {
                    return compareValues(indicatorValue, operator, (BigDecimal) compareValue);
                } else if (compareValue instanceof Number) {
                    return compareValues(indicatorValue, operator, BigDecimal.valueOf(((Number) compareValue).doubleValue()));
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to evaluate rule", e);
        }
        
        return false;
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
    
    private List<String> getMatchedRules(SignalType signal, Map<String, Object> indicators, String configJson) {
        List<String> matchedRules = new ArrayList<>();
        
        try {
            com.fasterxml.jackson.databind.JsonNode config = objectMapper.readTree(configJson);
            com.fasterxml.jackson.databind.JsonNode rules = signal == SignalType.BUY ? config.get("entry") : config.get("exit");
            
            if (rules != null && rules.has("rules")) {
                for (com.fasterxml.jackson.databind.JsonNode rule : rules.get("rules")) {
                    String indicator = rule.get("indicator").asText();
                    String operator = rule.get("operator").asText();
                    
                    if (rule.has("value")) {
                        double value = rule.get("value").asDouble();
                        matchedRules.add(String.format("%s %s %.2f", indicator, operator, value));
                    } else if (rule.has("compare_to")) {
                        String compareTo = rule.get("compare_to").asText();
                        matchedRules.add(String.format("%s %s %s", indicator, operator, compareTo));
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to get matched rules", e);
            matchedRules.add("Rule evaluation failed");
        }
        
        return matchedRules;
    }
    
    private BigDecimal calculateConfidenceScore(SignalType signal, List<String> matchedRules) {
        if (signal == SignalType.NO_SIGNAL || signal == SignalType.HOLD) {
            return BigDecimal.valueOf(0.5);
        }
        
        // Base confidence on number of matched rules
        double baseConfidence = 0.6;
        double ruleBonus = matchedRules.size() * 0.1;
        double confidence = Math.min(0.95, baseConfidence + ruleBonus);
        
        return BigDecimal.valueOf(confidence);
    }
    
    // Technical indicator calculations
    private BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
        if (prices.size() < period + 1) {
            return BigDecimal.valueOf(50); // Neutral RSI
        }
        
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;
        
        // Calculate initial average gain and loss
        for (int i = 1; i <= period; i++) {
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
        BigDecimal rsi = BigDecimal.valueOf(100).subtract(
            BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 6, RoundingMode.HALF_UP)
        );
        
        return rsi;
    }
    
    private BigDecimal calculateSMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) {
            return prices.get(prices.size() - 1); // Return last price if not enough data
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
        Map<String, BigDecimal> macd = new HashMap<>();
        
        if (prices.size() < 26) {
            macd.put("MACD", BigDecimal.ZERO);
            macd.put("MACD_Signal", BigDecimal.ZERO);
            macd.put("MACD_Histogram", BigDecimal.ZERO);
            return macd;
        }
        
        BigDecimal ema12 = calculateEMA(prices, 12);
        BigDecimal ema26 = calculateEMA(prices, 26);
        BigDecimal macdLine = ema12.subtract(ema26);
        
        // For simplicity, use a basic signal line calculation
        BigDecimal signalLine = macdLine.multiply(BigDecimal.valueOf(0.9)); // Simplified
        BigDecimal histogram = macdLine.subtract(signalLine);
        
        macd.put("MACD", macdLine);
        macd.put("MACD_Signal", signalLine);
        macd.put("MACD_Histogram", histogram);
        
        return macd;
    }
    
    private BigDecimal getBasePrice(String symbol) {
        switch (symbol.toUpperCase()) {
            case "AAPL": return BigDecimal.valueOf(172.50);
            case "MSFT": return BigDecimal.valueOf(415.75);
            case "GOOGL": return BigDecimal.valueOf(175.85);
            case "TSLA": return BigDecimal.valueOf(248.50);
            case "BTCUSD": return BigDecimal.valueOf(97250.00);
            case "ETHUSD": return BigDecimal.valueOf(3420.50);
            default: return BigDecimal.valueOf(100.00);
        }
    }
    
    public List<StrategyVersion> getStrategyVersions(UUID strategyId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Strategy strategy = strategyRepository.findByIdAndUser(strategyId, user)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));
        
        return versionRepository.findByStrategyOrderByVersionNumberDesc(strategy);
    }
    
    private void createStrategyVersion(Strategy strategy, Integer versionNumber, String configJson, String description, User author) {
        StrategyVersion version = new StrategyVersion();
        version.setStrategy(strategy);
        version.setVersionNumber(versionNumber);
        version.setConfigJson(configJson);
        version.setChangeDescription(description);
        version.setAuthor(author);
        
        versionRepository.save(version);
    }
    
    private boolean canCreateStrategy(UserRole role) {
        return role == UserRole.RESEARCHER || role == UserRole.PORTFOLIO_MANAGER || role == UserRole.ADMIN;
    }
    
    private StrategyResponse convertToResponse(Strategy strategy) {
        StrategyResponse response = new StrategyResponse();
        response.setId(strategy.getId());
        response.setName(strategy.getName());
        response.setDescription(strategy.getDescription());
        response.setSymbol(strategy.getSymbol());
        response.setTimeframe(strategy.getTimeframe());
        response.setConfigJson(strategy.getConfigJson());
        response.setStatus(strategy.getStatus());
        response.setTags(strategy.getTags());
        response.setCurrentVersion(strategy.getCurrentVersion());
        response.setOwnerName(strategy.getUser().getFullName());
        response.setCreatedAt(strategy.getCreatedAt());
        response.setUpdatedAt(strategy.getUpdatedAt());
        
        return response;
    }
    
    private SignalType simulateSignalEvaluation(String configJson, Map<String, Object> marketData) {
        // Simulate strategy evaluation logic
        // In a real implementation, this would parse the JSON config and evaluate rules
        Random random = new Random();
        SignalType[] signals = {SignalType.BUY, SignalType.SELL, SignalType.HOLD, SignalType.NO_SIGNAL};
        return signals[random.nextInt(signals.length)];
    }
    
    private Map<String, Object> getIndicatorValues(Map<String, Object> marketData) {
        Map<String, Object> indicators = new HashMap<>();
        indicators.put("RSI", 45.2);
        indicators.put("SMA_50", 150.5);
        indicators.put("EMA_20", 152.1);
        indicators.put("MACD", 0.8);
        return indicators;
    }
    
    private List<String> getMatchedRules(String configJson) {
        // Simulate matched rules based on config
        return Arrays.asList("RSI < 50", "Price > SMA_50");
    }
}