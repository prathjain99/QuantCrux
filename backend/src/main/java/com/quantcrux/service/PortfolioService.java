package com.quantcrux.service;

import com.quantcrux.dto.*;
import com.quantcrux.model.*;
import com.quantcrux.repository.*;
import com.quantcrux.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortfolioService {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);
    
    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private PortfolioHoldingRepository holdingRepository;
    
    @Autowired
    private PortfolioHistoryRepository historyRepository;
    
    @Autowired
    private PortfolioTransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MarketDataService marketDataService;
    
    public List<PortfolioResponse> getUserPortfolios(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        List<Portfolio> portfolios = portfolioRepository.findByOwnerOrManagerOrderByCreatedAtDesc(user);
        
        return portfolios.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public PortfolioResponse getPortfolio(UUID portfolioId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Portfolio portfolio = portfolioRepository.findByIdAndUser(portfolioId, user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        return convertToDetailedResponse(portfolio);
    }
    
    public PortfolioResponse createPortfolio(PortfolioRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        
        // Get manager if specified
        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            
            // Validate manager role
            if (manager.getRole() != UserRole.PORTFOLIO_MANAGER && manager.getRole() != UserRole.ADMIN) {
                throw new RuntimeException("Assigned manager must have PORTFOLIO_MANAGER or ADMIN role");
            }
        }
        
        // Create portfolio
        Portfolio portfolio = new Portfolio();
        portfolio.setOwner(user);
        portfolio.setManager(manager);
        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());
        portfolio.setInitialCapital(request.getInitialCapital());
        portfolio.setCurrentNav(request.getInitialCapital());
        portfolio.setCashBalance(request.getInitialCapital());
        portfolio.setStatus(request.getStatus());
        portfolio.setCurrency(request.getCurrency());
        portfolio.setBenchmarkSymbol(request.getBenchmarkSymbol());
        
        portfolio = portfolioRepository.save(portfolio);
        
        // Create initial deposit transaction
        PortfolioTransaction initialDeposit = new PortfolioTransaction();
        initialDeposit.setPortfolio(portfolio);
        initialDeposit.setTransactionType(TransactionType.DEPOSIT);
        initialDeposit.setAmount(request.getInitialCapital());
        initialDeposit.setDescription("Initial capital deposit");
        transactionRepository.save(initialDeposit);
        
        // Create initial history entry
        PortfolioHistory initialHistory = new PortfolioHistory();
        initialHistory.setPortfolio(portfolio);
        initialHistory.setDate(LocalDate.now());
        initialHistory.setNav(request.getInitialCapital());
        initialHistory.setTotalReturnPct(BigDecimal.ZERO);
        initialHistory.setDailyReturnPct(BigDecimal.ZERO);
        initialHistory.setContributions(request.getInitialCapital());
        historyRepository.save(initialHistory);
        
        return convertToResponse(portfolio);
    }
    
    public PortfolioResponse updatePortfolio(UUID portfolioId, PortfolioRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Portfolio portfolio = portfolioRepository.findByIdAndUser(portfolioId, user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        // Get manager if specified
        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
        }
        
        portfolio.setManager(manager);
        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());
        portfolio.setStatus(request.getStatus());
        portfolio.setCurrency(request.getCurrency());
        portfolio.setBenchmarkSymbol(request.getBenchmarkSymbol());
        
        portfolio = portfolioRepository.save(portfolio);
        return convertToResponse(portfolio);
    }
    
    public void deletePortfolio(UUID portfolioId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Portfolio portfolio = portfolioRepository.findByIdAndUser(portfolioId, user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        // Check if portfolio can be deleted
        if (portfolio.getStatus() == PortfolioStatus.ACTIVE) {
            List<PortfolioHolding> activeHoldings = holdingRepository.findByPortfolio(portfolio)
                    .stream()
                    .filter(h -> h.getQuantity().compareTo(BigDecimal.ZERO) != 0)
                    .collect(Collectors.toList());
            
            if (!activeHoldings.isEmpty()) {
                throw new RuntimeException("Cannot delete portfolio with active holdings");
            }
        }
        
        portfolioRepository.delete(portfolio);
    }
    
    public PortfolioResponse updatePortfolioMetrics(UUID portfolioId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Portfolio portfolio = portfolioRepository.findByIdAndUser(portfolioId, user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        // Update holdings with latest prices
        updateHoldingPrices(portfolio);
        
        // Recalculate portfolio metrics
        calculatePortfolioMetrics(portfolio);
        
        // Save updated portfolio
        portfolioRepository.save(portfolio);
        
        return convertToDetailedResponse(portfolio);
    }
    
    public List<PortfolioResponse.NAVPoint> getPortfolioNAVHistory(UUID portfolioId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Portfolio portfolio = portfolioRepository.findByIdAndUser(portfolioId, user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        List<PortfolioHistory> history = historyRepository.findByPortfolioOrderByDateDesc(portfolio);
        
        return history.stream()
                .map(h -> {
                    PortfolioResponse.NAVPoint point = new PortfolioResponse.NAVPoint();
                    point.setDate(h.getDate().toString());
                    point.setNav(h.getNav());
                    point.setDailyReturn(h.getDailyReturnPct());
                    return point;
                })
                .collect(Collectors.toList());
    }
    
    private void updateHoldingPrices(Portfolio portfolio) {
        List<PortfolioHolding> holdings = holdingRepository.findByPortfolio(portfolio);
        
        for (PortfolioHolding holding : holdings) {
            try {
                // Get latest market data using the new MarketDataService
                MarketDataResponse marketData = marketDataService.getLivePrice(holding.getSymbol());
                BigDecimal latestPrice = marketData.getPrice();
                
                if (latestPrice == null) {
                    logger.warn("No current price available for {}, skipping update", holding.getSymbol());
                    continue;
                }
                
                // Update holding with latest price
                holding.setLatestPrice(latestPrice);
                holding.setMarketValue(holding.getQuantity().multiply(latestPrice));
                holding.setUnrealizedPnl(holding.getMarketValue().subtract(holding.getCostBasis()));
                
                // Calculate weight percentage
                if (portfolio.getCurrentNav().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal weight = holding.getMarketValue()
                            .divide(portfolio.getCurrentNav(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    holding.setWeightPct(weight);
                }
                
                holdingRepository.save(holding);
                
            } catch (Exception e) {
                logger.error("Failed to update price for holding {} in portfolio {}", 
                           holding.getSymbol(), portfolio.getId(), e);
            }
        }
    }
    
    private void calculatePortfolioMetrics(Portfolio portfolio) {
        List<PortfolioHolding> holdings = holdingRepository.findByPortfolio(portfolio);
        
        // Calculate total market value
        BigDecimal totalMarketValue = holdings.stream()
                .filter(h -> h.getMarketValue() != null)
                .map(PortfolioHolding::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total P&L
        BigDecimal totalPnl = holdings.stream()
                .filter(h -> h.getUnrealizedPnl() != null)
                .map(PortfolioHolding::getUnrealizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Update portfolio NAV
        BigDecimal currentNav = totalMarketValue.add(portfolio.getCashBalance());
        portfolio.setCurrentNav(currentNav);
        portfolio.setTotalPnl(totalPnl);
        
        // Calculate total return percentage
        if (portfolio.getInitialCapital().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalReturn = currentNav.subtract(portfolio.getInitialCapital())
                    .divide(portfolio.getInitialCapital(), 6, RoundingMode.HALF_UP);
            portfolio.setTotalReturnPct(totalReturn);
        }
        
        // Calculate risk metrics (simplified)
        calculateRiskMetrics(portfolio);
    }
    
    private void calculateRiskMetrics(Portfolio portfolio) {
        // Get historical returns for risk calculation
        LocalDate fromDate = LocalDate.now().minusDays(30);
        List<PortfolioHistory> history = historyRepository.findByPortfolioAndDateAfter(portfolio, fromDate);
        
        if (history.size() < 2) {
            // Calculate basic metrics from current holdings if no history
            calculateBasicRiskMetrics(portfolio);
            return;
        }
        
        List<BigDecimal> returns = history.stream()
                .filter(h -> h.getDailyReturnPct() != null)
                .map(PortfolioHistory::getDailyReturnPct)
                .collect(Collectors.toList());
        
        if (returns.isEmpty()) {
            calculateBasicRiskMetrics(portfolio);
            return;
        }
        
        // Calculate portfolio volatility (annualized)
        BigDecimal avgReturn = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(avgReturn).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size() - 1), 6, RoundingMode.HALF_UP); // Sample variance
        
        BigDecimal volatility = BigDecimal.valueOf(Math.sqrt(variance.doubleValue() * 252)); // Annualized
        portfolio.setVolatility(volatility);
        
        // Calculate Value at Risk (95% confidence, 1-day)
        List<BigDecimal> sortedReturns = returns.stream()
                .sorted()
                .collect(Collectors.toList());
        
        int varIndex = (int) (sortedReturns.size() * 0.05);
        if (varIndex < sortedReturns.size()) {
            BigDecimal varReturn = sortedReturns.get(varIndex);
            BigDecimal var95 = portfolio.getCurrentNav().multiply(varReturn.abs());
            portfolio.setVar95(var95);
        }
        
        // Calculate Sharpe ratio (annualized)
        BigDecimal annualizedReturn = avgReturn.multiply(BigDecimal.valueOf(252));
        BigDecimal riskFreeRate = BigDecimal.valueOf(0.05); // 5% annual risk-free rate
        
        if (volatility.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sharpeRatio = annualizedReturn.subtract(riskFreeRate).divide(volatility, 6, RoundingMode.HALF_UP);
            portfolio.setSharpeRatio(sharpeRatio);
        }
        
        // Calculate beta against benchmark
        calculatePortfolioBeta(portfolio, returns);
        
        // Calculate max drawdown
        calculateMaxDrawdown(portfolio, history);
    }
    
    private void calculateBasicRiskMetrics(Portfolio portfolio) {
        try {
            // Calculate portfolio-level volatility from holdings
            List<PortfolioHolding> holdings = holdingRepository.findByPortfolio(portfolio);
            
            if (holdings.isEmpty()) {
                return;
            }
            
            BigDecimal portfolioVolatility = BigDecimal.ZERO;
            BigDecimal totalWeight = BigDecimal.ZERO;
            
            for (PortfolioHolding holding : holdings) {
                if (holding.getWeightPct() != null && holding.getWeightPct().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal weight = holding.getWeightPct().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                    BigDecimal assetVolatility = getAssetVolatility(holding.getSymbol());
                    
                    portfolioVolatility = portfolioVolatility.add(weight.multiply(assetVolatility));
                    totalWeight = totalWeight.add(weight);
                }
            }
            
            if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                portfolioVolatility = portfolioVolatility.divide(totalWeight, 6, RoundingMode.HALF_UP);
                portfolio.setVolatility(portfolioVolatility);
                
                // Estimate VaR based on volatility (assuming normal distribution)
                BigDecimal var95 = portfolio.getCurrentNav().multiply(portfolioVolatility)
                        .multiply(BigDecimal.valueOf(1.645)) // 95% confidence z-score
                        .divide(BigDecimal.valueOf(Math.sqrt(252)), 6, RoundingMode.HALF_UP); // Daily VaR
                portfolio.setVar95(var95);
            }
            
        } catch (Exception e) {
            logger.error("Failed to calculate basic risk metrics", e);
        }
    }
    
    private BigDecimal getAssetVolatility(String symbol) {
        try {
            // Get 30 days of historical data to calculate volatility
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(30);
            
            MarketDataResponse historicalData = marketDataService.getOHLCVData(symbol, "1d", startTime, endTime);
            
            if (historicalData.getOhlcvData() != null && historicalData.getOhlcvData().size() > 1) {
                List<BigDecimal> returns = new ArrayList<>();
                List<MarketDataResponse.OHLCVData> ohlcvData = historicalData.getOhlcvData();
                
                for (int i = 1; i < ohlcvData.size(); i++) {
                    BigDecimal prevClose = ohlcvData.get(i - 1).getClose();
                    BigDecimal currentClose = ohlcvData.get(i).getClose();
                    
                    if (prevClose.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal logReturn = BigDecimal.valueOf(Math.log(currentClose.divide(prevClose, 10, RoundingMode.HALF_UP).doubleValue()));
                        returns.add(logReturn);
                    }
                }
                
                if (returns.size() > 1) {
                    BigDecimal mean = returns.stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(returns.size()), 10, RoundingMode.HALF_UP);
                    
                    BigDecimal variance = returns.stream()
                            .map(r -> r.subtract(mean).pow(2))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(returns.size() - 1), 10, RoundingMode.HALF_UP);
                    
                    // Annualized volatility
                    return BigDecimal.valueOf(Math.sqrt(variance.doubleValue() * 252));
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to calculate volatility for {}", symbol, e);
        }
        
        // Fallback to default volatilities by asset type
        switch (symbol.toUpperCase()) {
            case "BTCUSD":
            case "ETHUSD":
                return BigDecimal.valueOf(0.60); // 60% for crypto
            case "TSLA":
                return BigDecimal.valueOf(0.40); // 40% for volatile stocks
            case "SPY":
            case "QQQ":
                return BigDecimal.valueOf(0.15); // 15% for ETFs
            default:
                return BigDecimal.valueOf(0.25); // 25% for regular stocks
        }
    }
    
    private void calculatePortfolioBeta(Portfolio portfolio, List<BigDecimal> portfolioReturns) {
        try {
            // Get benchmark returns for the same period
            String benchmarkSymbol = portfolio.getBenchmarkSymbol();
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(portfolioReturns.size() + 5); // Extra buffer
            
            MarketDataResponse benchmarkData = marketDataService.getOHLCVData(benchmarkSymbol, "1d", startTime, endTime);
            
            if (benchmarkData.getOhlcvData() != null && benchmarkData.getOhlcvData().size() > 1) {
                List<BigDecimal> benchmarkReturns = new ArrayList<>();
                List<MarketDataResponse.OHLCVData> ohlcvData = benchmarkData.getOhlcvData();
                
                for (int i = 1; i < ohlcvData.size(); i++) {
                    BigDecimal prevClose = ohlcvData.get(i - 1).getClose();
                    BigDecimal currentClose = ohlcvData.get(i).getClose();
                    
                    if (prevClose.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal dailyReturn = currentClose.subtract(prevClose).divide(prevClose, 6, RoundingMode.HALF_UP);
                        benchmarkReturns.add(dailyReturn);
                    }
                }
                
                // Align return series
                int minLength = Math.min(portfolioReturns.size(), benchmarkReturns.size());
                if (minLength > 5) {
                    List<BigDecimal> alignedPortfolioReturns = portfolioReturns.subList(0, minLength);
                    List<BigDecimal> alignedBenchmarkReturns = benchmarkReturns.subList(benchmarkReturns.size() - minLength, benchmarkReturns.size());
                    
                    // Calculate beta = Cov(portfolio, benchmark) / Var(benchmark)
                    BigDecimal covariance = calculateCovariance(alignedPortfolioReturns, alignedBenchmarkReturns);
                    BigDecimal benchmarkVariance = calculateVariance(alignedBenchmarkReturns);
                    
                    if (benchmarkVariance.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal beta = covariance.divide(benchmarkVariance, 6, RoundingMode.HALF_UP);
                        portfolio.setBeta(beta);
                        
                        logger.info("Calculated portfolio beta: {} vs {}", beta, benchmarkSymbol);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to calculate portfolio beta", e);
        }
    }
    
    private BigDecimal calculateCovariance(List<BigDecimal> returns1, List<BigDecimal> returns2) {
        if (returns1.size() != returns2.size() || returns1.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal mean1 = returns1.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns1.size()), 6, RoundingMode.HALF_UP);
        BigDecimal mean2 = returns2.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns2.size()), 6, RoundingMode.HALF_UP);
        
        BigDecimal covariance = BigDecimal.ZERO;
        for (int i = 0; i < returns1.size(); i++) {
            BigDecimal diff1 = returns1.get(i).subtract(mean1);
            BigDecimal diff2 = returns2.get(i).subtract(mean2);
            covariance = covariance.add(diff1.multiply(diff2));
        }
        
        return covariance.divide(BigDecimal.valueOf(returns1.size() - 1), 6, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateVariance(List<BigDecimal> returns) {
        if (returns.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal mean = returns.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size() - 1), 6, RoundingMode.HALF_UP);
        
        return variance;
    }
    
    private void calculateMaxDrawdown(Portfolio portfolio, List<PortfolioHistory> history) {
        if (history.size() < 2) {
            return;
        }
        
        BigDecimal peak = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        int maxDrawdownDuration = 0;
        int currentDrawdownDuration = 0;
        
        for (PortfolioHistory point : history) {
            if (point.getNav().compareTo(peak) > 0) {
                peak = point.getNav();
                currentDrawdownDuration = 0;
            } else {
                currentDrawdownDuration++;
            }
            
            if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = peak.subtract(point.getNav()).divide(peak, 6, RoundingMode.HALF_UP);
                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                    maxDrawdownDuration = currentDrawdownDuration;
                }
            }
        }
        
        portfolio.setMaxDrawdown(maxDrawdown);
    }
    
    private boolean canCreatePortfolio(UserRole role) {
        return role == UserRole.CLIENT || role == UserRole.PORTFOLIO_MANAGER || role == UserRole.ADMIN;
    }
    
    private PortfolioResponse convertToResponse(Portfolio portfolio) {
        PortfolioResponse response = new PortfolioResponse();
        response.setId(portfolio.getId());
        response.setName(portfolio.getName());
        response.setDescription(portfolio.getDescription());
        response.setOwnerName(portfolio.getOwner().getFullName());
        response.setManagerName(portfolio.getManager() != null ? portfolio.getManager().getFullName() : null);
        response.setOwnerId(portfolio.getOwner().getId());
        response.setManagerId(portfolio.getManager() != null ? portfolio.getManager().getId() : null);
        response.setInitialCapital(portfolio.getInitialCapital());
        response.setCurrentNav(portfolio.getCurrentNav());
        response.setCashBalance(portfolio.getCashBalance());
        response.setTotalPnl(portfolio.getTotalPnl());
        response.setTotalReturnPct(portfolio.getTotalReturnPct());
        response.setVar95(portfolio.getVar95());
        response.setVolatility(portfolio.getVolatility());
        response.setBeta(portfolio.getBeta());
        response.setMaxDrawdown(portfolio.getMaxDrawdown());
        response.setSharpeRatio(portfolio.getSharpeRatio());
        response.setStatus(portfolio.getStatus());
        response.setCurrency(portfolio.getCurrency());
        response.setBenchmarkSymbol(portfolio.getBenchmarkSymbol());
        response.setCreatedAt(portfolio.getCreatedAt());
        response.setUpdatedAt(portfolio.getUpdatedAt());
        
        return response;
    }
    
    private PortfolioResponse convertToDetailedResponse(Portfolio portfolio) {
        PortfolioResponse response = convertToResponse(portfolio);
        
        // Add holdings
        List<PortfolioHolding> holdings = holdingRepository.findByPortfolioOrderByWeightPctDesc(portfolio);
        List<PortfolioResponse.HoldingResponse> holdingResponses = holdings.stream()
                .map(this::convertHoldingToResponse)
                .collect(Collectors.toList());
        response.setHoldings(holdingResponses);
        
        // Add NAV history (last 30 days)
        LocalDate fromDate = LocalDate.now().minusDays(30);
        List<PortfolioHistory> history = historyRepository.findByPortfolioAndDateAfter(portfolio, fromDate);
        List<PortfolioResponse.NAVPoint> navHistory = history.stream()
                .map(h -> {
                    PortfolioResponse.NAVPoint point = new PortfolioResponse.NAVPoint();
                    point.setDate(h.getDate().toString());
                    point.setNav(h.getNav());
                    point.setDailyReturn(h.getDailyReturnPct());
                    return point;
                })
                .collect(Collectors.toList());
        response.setNavHistory(navHistory);
        
        // Add recent transactions
        List<PortfolioTransaction> transactions = transactionRepository.findByPortfolioOrderByExecutedAtDesc(portfolio)
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        List<PortfolioResponse.TransactionResponse> transactionResponses = transactions.stream()
                .map(this::convertTransactionToResponse)
                .collect(Collectors.toList());
        response.setRecentTransactions(transactionResponses);
        
        // Add allocation breakdowns
        response.setAssetAllocation(calculateAssetAllocation(portfolio));
        response.setSectorAllocation(calculateSectorAllocation(portfolio));
        
        return response;
    }
    
    private PortfolioResponse.HoldingResponse convertHoldingToResponse(PortfolioHolding holding) {
        PortfolioResponse.HoldingResponse response = new PortfolioResponse.HoldingResponse();
        response.setId(holding.getId());
        response.setInstrumentType(holding.getInstrumentType().name());
        response.setSymbol(holding.getSymbol());
        response.setQuantity(holding.getQuantity());
        response.setAvgPrice(holding.getAvgPrice());
        response.setLatestPrice(holding.getLatestPrice());
        response.setMarketValue(holding.getMarketValue());
        response.setCostBasis(holding.getCostBasis());
        response.setUnrealizedPnl(holding.getUnrealizedPnl());
        response.setRealizedPnl(holding.getRealizedPnl());
        response.setSector(holding.getSector());
        response.setAssetClass(holding.getAssetClass());
        response.setWeightPct(holding.getWeightPct());
        
        return response;
    }
    
    private PortfolioResponse.TransactionResponse convertTransactionToResponse(PortfolioTransaction transaction) {
        PortfolioResponse.TransactionResponse response = new PortfolioResponse.TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionType(transaction.getTransactionType().name());
        response.setSymbol(transaction.getSymbol());
        response.setQuantity(transaction.getQuantity());
        response.setPrice(transaction.getPrice());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setExecutedAt(transaction.getExecutedAt());
        
        return response;
    }
    
    private List<PortfolioResponse.AllocationBreakdown> calculateAssetAllocation(Portfolio portfolio) {
        List<Object[]> results = holdingRepository.getAssetClassAllocationByPortfolio(portfolio);
        BigDecimal totalValue = portfolio.getCurrentNav();
        
        return results.stream()
                .map(result -> {
                    String assetClass = (String) result[0];
                    BigDecimal value = (BigDecimal) result[1];
                    BigDecimal percentage = value.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    
                    PortfolioResponse.AllocationBreakdown breakdown = new PortfolioResponse.AllocationBreakdown();
                    breakdown.setCategory(assetClass);
                    breakdown.setValue(value);
                    breakdown.setPercentage(percentage);
                    return breakdown;
                })
                .collect(Collectors.toList());
    }
    
    private List<PortfolioResponse.AllocationBreakdown> calculateSectorAllocation(Portfolio portfolio) {
        List<Object[]> results = holdingRepository.getSectorAllocationByPortfolio(portfolio);
        BigDecimal totalValue = portfolio.getCurrentNav();
        
        return results.stream()
                .map(result -> {
                    String sector = (String) result[0];
                    BigDecimal value = (BigDecimal) result[1];
                    BigDecimal percentage = value.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    
                    PortfolioResponse.AllocationBreakdown breakdown = new PortfolioResponse.AllocationBreakdown();
                    breakdown.setCategory(sector);
                    breakdown.setValue(value);
                    breakdown.setPercentage(percentage);
                    return breakdown;
                })
                .collect(Collectors.toList());
    }
}