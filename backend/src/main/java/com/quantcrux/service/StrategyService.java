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
            // Get market data for the symbol
            Map<String, Object> marketData = marketDataService.getMarketData(request.getSymbol(), request.getTimeframe());
            
            // Parse strategy configuration
            // In a real implementation, you would parse the JSON and evaluate the rules
            // For now, we'll simulate the evaluation
            
            SignalEvaluationResponse response = new SignalEvaluationResponse();
            response.setSignal(simulateSignalEvaluation(request.getConfigJson(), marketData));
            response.setCurrentPrice((BigDecimal) marketData.get("price"));
            response.setIndicatorValues(getIndicatorValues(marketData));
            response.setMatchedRules(getMatchedRules(request.getConfigJson()));
            response.setConfidenceScore(BigDecimal.valueOf(0.75));
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