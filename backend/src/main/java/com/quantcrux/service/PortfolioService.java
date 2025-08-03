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
                // Get latest market data
                Map<String, Object> marketData = marketDataService.getMarketData(holding.getSymbol(), "1d");
                BigDecimal latestPrice = (BigDecimal) marketData.get("price");
                
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
            return; // Not enough data for risk metrics
        }
        
        List<BigDecimal> returns = history.stream()
                .filter(h -> h.getDailyReturnPct() != null)
                .map(PortfolioHistory::getDailyReturnPct)
                .collect(Collectors.toList());
        
        if (returns.isEmpty()) {
            return;
        }
        
        // Calculate volatility (standard deviation of returns)
        BigDecimal avgReturn = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(avgReturn).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        BigDecimal volatility = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        portfolio.setVolatility(volatility);
        
        // Calculate VaR (95% confidence, 1-day)
        List<BigDecimal> sortedReturns = returns.stream()
                .sorted()
                .collect(Collectors.toList());
        
        int varIndex = (int) (sortedReturns.size() * 0.05);
        if (varIndex < sortedReturns.size()) {
            BigDecimal varReturn = sortedReturns.get(varIndex);
            BigDecimal var95 = portfolio.getCurrentNav().multiply(varReturn.abs());
            portfolio.setVar95(var95);
        }
        
        // Simplified Sharpe ratio (assuming 5% risk-free rate)
        BigDecimal riskFreeRate = BigDecimal.valueOf(0.05).divide(BigDecimal.valueOf(252), 6, RoundingMode.HALF_UP); // Daily
        if (volatility.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sharpeRatio = avgReturn.subtract(riskFreeRate).divide(volatility, 6, RoundingMode.HALF_UP);
            portfolio.setSharpeRatio(sharpeRatio);
        }
        
        // Calculate max drawdown
        calculateMaxDrawdown(portfolio, history);
    }
    
    private void calculateMaxDrawdown(Portfolio portfolio, List<PortfolioHistory> history) {
        if (history.size() < 2) {
            return;
        }
        
        BigDecimal peak = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        
        for (PortfolioHistory point : history) {
            if (point.getNav().compareTo(peak) > 0) {
                peak = point.getNav();
            }
            
            BigDecimal drawdown = peak.subtract(point.getNav()).divide(peak, 6, RoundingMode.HALF_UP);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
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