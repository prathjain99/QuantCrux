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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    
    @Autowired
    private AnalyticsRiskRepository riskRepository;
    
    @Autowired
    private AnalyticsPerformanceRepository performanceRepository;
    
    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private StrategyRepository strategyRepository;
    
    @Autowired
    private PortfolioHistoryRepository portfolioHistoryRepository;
    
    @Autowired
    private TradeRepository tradeRepository;
    
    @Autowired
    private MarketDataService marketDataService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    
    public AnalyticsResponse getPortfolioAnalytics(UUID portfolioId, AnalyticsRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Portfolio portfolio = portfolioRepository.findByIdAndUser(portfolioId, user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        // Calculate or retrieve analytics
        AnalyticsResponse response = calculateAnalytics(portfolio, null, request);
        
        return response;
    }
    
    public AnalyticsResponse getStrategyAnalytics(UUID strategyId, AnalyticsRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Strategy strategy = strategyRepository.findByIdAndUser(strategyId, user)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));
        
        // Calculate or retrieve analytics
        AnalyticsResponse response = calculateAnalytics(null, strategy, request);
        
        return response;
    }
    
    public ReportResponse generateReport(ReportRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        
        // Validate portfolio/strategy access
        Portfolio portfolio = null;
        Strategy strategy = null;
        
        if (request.getPortfolioId() != null) {
            portfolio = portfolioRepository.findByIdAndUser(request.getPortfolioId(), user)
                    .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        }
        
        if (request.getStrategyId() != null) {
            strategy = strategyRepository.findByIdAndUser(request.getStrategyId(), user)
                    .orElseThrow(() -> new RuntimeException("Strategy not found"));
        }
        
        // Create report record
        Report report = new Report();
        report.setUser(user);
        report.setPortfolio(portfolio);
        report.setStrategy(strategy);
        report.setReportType(request.getReportType());
        report.setReportName(request.getReportName());
        report.setDescription(request.getDescription());
        report.setPeriodStart(request.getPeriodStart());
        report.setPeriodEnd(request.getPeriodEnd());
        report.setFileFormat(request.getFileFormat());
        report.setTemplateConfig(request.getTemplateConfig());
        report.setFilters(request.getFilters());
        
        report = reportRepository.save(report);
        
        // Generate report asynchronously
        generateReportAsync(report, request);
        
        return convertReportToResponse(report);
    }
    
    public List<ReportResponse> getUserReports(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        List<Report> reports = reportRepository.findByUserOrderByCreatedAtDesc(user);
        
        return reports.stream()
                .map(this::convertReportToResponse)
                .collect(Collectors.toList());
    }
    
    public ReportResponse getReport(UUID reportId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        if (!report.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        return convertReportToResponse(report);
    }
    
    public void deleteReport(UUID reportId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        if (!report.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        reportRepository.delete(report);
    }
    
    private AnalyticsResponse calculateAnalytics(Portfolio portfolio, Strategy strategy, AnalyticsRequest request) {
        AnalyticsResponse response = new AnalyticsResponse();
        
        if (portfolio != null) {
            response.setPortfolioId(portfolio.getId());
            response.setPortfolioName(portfolio.getName());
            
            // Calculate portfolio analytics
            calculatePortfolioRiskMetrics(portfolio, response, request);
            calculatePortfolioPerformanceMetrics(portfolio, response, request);
            
            if (request.getIncludeAttribution()) {
                calculateAttributionAnalysis(portfolio, response, request);
            }
            
            if (request.getIncludeCorrelations()) {
                calculateCorrelationMatrix(portfolio, response, request);
            }
            
        } else if (strategy != null) {
            response.setStrategyId(strategy.getId());
            response.setStrategyName(strategy.getName());
            
            // Calculate strategy analytics
            calculateStrategyRiskMetrics(strategy, response, request);
            calculateStrategyPerformanceMetrics(strategy, response, request);
        }
        
        response.setPeriodStart(request.getPeriodStart());
        response.setPeriodEnd(request.getPeriodEnd());
        response.setBenchmarkSymbol(request.getBenchmarkSymbol());
        
        return response;
    }
    
    private void calculatePortfolioRiskMetrics(Portfolio portfolio, AnalyticsResponse response, AnalyticsRequest request) {
        // Get historical NAV data
        List<PortfolioHistory> history = portfolioHistoryRepository.findByPortfolioAndDateBetween(
            portfolio, request.getPeriodStart(), request.getPeriodEnd());
        
        if (history.size() < 2) {
            // Not enough data for risk calculations
            return;
        }
        
        // Calculate daily returns
        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < history.size(); i++) {
            PortfolioHistory prev = history.get(i - 1);
            PortfolioHistory curr = history.get(i);
            
            if (prev.getNav().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal dailyReturn = curr.getNav().subtract(prev.getNav())
                        .divide(prev.getNav(), 6, RoundingMode.HALF_UP);
                returns.add(dailyReturn);
            }
        }
        
        if (returns.isEmpty()) {
            return;
        }
        
        // Calculate volatility
        BigDecimal avgReturn = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(avgReturn).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        BigDecimal volatility = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()) * Math.sqrt(252)); // Annualized
        response.setVolatility(volatility);
        
        // Calculate VaR (95% and 99%)
        List<BigDecimal> sortedReturns = returns.stream().sorted().collect(Collectors.toList());
        int var95Index = (int) (sortedReturns.size() * 0.05);
        int var99Index = (int) (sortedReturns.size() * 0.01);
        
        if (var95Index < sortedReturns.size()) {
            BigDecimal var95Return = sortedReturns.get(var95Index);
            BigDecimal var95 = portfolio.getCurrentNav().multiply(var95Return.abs());
            response.setVar95(var95);
        }
        
        if (var99Index < sortedReturns.size()) {
            BigDecimal var99Return = sortedReturns.get(var99Index);
            BigDecimal var99 = portfolio.getCurrentNav().multiply(var99Return.abs());
            response.setVar99(var99);
        }
        
        // Calculate Sharpe ratio (assuming 5% risk-free rate)
        BigDecimal riskFreeRate = BigDecimal.valueOf(0.05).divide(BigDecimal.valueOf(252), 6, RoundingMode.HALF_UP);
        if (volatility.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dailyVolatility = volatility.divide(BigDecimal.valueOf(Math.sqrt(252)), 6, RoundingMode.HALF_UP);
            BigDecimal sharpeRatio = avgReturn.subtract(riskFreeRate).divide(dailyVolatility, 6, RoundingMode.HALF_UP);
            response.setSharpeRatio(sharpeRatio);
        }
        
        // Calculate max drawdown
        calculateMaxDrawdown(history, response);
        
        // Calculate benchmark metrics
        calculateBenchmarkMetrics(portfolio, response, request, returns);
    }
    
    private void calculatePortfolioPerformanceMetrics(Portfolio portfolio, AnalyticsResponse response, AnalyticsRequest request) {
        // Get trade data for the period
        List<Trade> trades = tradeRepository.findByPortfolioAndTradeDateAfter(portfolio, request.getPeriodStart());
        
        // Calculate trade metrics
        long winningTrades = trades.stream()
                .filter(t -> t.getStatus() == TradeStatus.EXECUTED)
                .filter(t -> {
                    // Simplified P&L calculation for demo
                    return random.nextBoolean(); // 50% win rate for demo
                })
                .count();
        
        response.setTotalTrades(trades.size());
        response.setWinningTrades((int) winningTrades);
        response.setLosingTrades(trades.size() - (int) winningTrades);
        
        if (trades.size() > 0) {
            BigDecimal winRate = BigDecimal.valueOf(winningTrades).divide(BigDecimal.valueOf(trades.size()), 6, RoundingMode.HALF_UP);
            response.setWinRate(winRate);
        }
        
        // Calculate returns
        if (portfolio.getInitialCapital().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalReturn = portfolio.getCurrentNav().subtract(portfolio.getInitialCapital())
                    .divide(portfolio.getInitialCapital(), 6, RoundingMode.HALF_UP);
            response.setTotalReturn(totalReturn);
            
            // Calculate CAGR
            long daysBetween = ChronoUnit.DAYS.between(request.getPeriodStart(), request.getPeriodEnd());
            if (daysBetween > 0) {
                double years = daysBetween / 365.0;
                double cagr = Math.pow(portfolio.getCurrentNav().divide(portfolio.getInitialCapital(), 6, RoundingMode.HALF_UP).doubleValue(), 1.0 / years) - 1.0;
                response.setCagr(BigDecimal.valueOf(cagr));
            }
        }
        
        // Calculate trade frequency
        long daysBetween = ChronoUnit.DAYS.between(request.getPeriodStart(), request.getPeriodEnd());
        if (daysBetween > 0) {
            BigDecimal tradeFrequency = BigDecimal.valueOf(trades.size() * 30.0 / daysBetween); // Trades per month
            response.setTradeFrequency(tradeFrequency);
        }
    }
    
    private void calculateStrategyRiskMetrics(Strategy strategy, AnalyticsResponse response, AnalyticsRequest request) {
        // For strategies, we would typically use backtest results
        // For now, use simplified calculations
        response.setVolatility(BigDecimal.valueOf(0.15 + random.nextGaussian() * 0.05));
        response.setSharpeRatio(BigDecimal.valueOf(1.0 + random.nextGaussian() * 0.5));
        response.setMaxDrawdown(BigDecimal.valueOf(-0.05 - random.nextDouble() * 0.15));
    }
    
    private void calculateStrategyPerformanceMetrics(Strategy strategy, AnalyticsResponse response, AnalyticsRequest request) {
        // Simplified strategy performance metrics
        response.setTotalReturn(BigDecimal.valueOf(0.08 + random.nextGaussian() * 0.10));
        response.setCagr(BigDecimal.valueOf(0.12 + random.nextGaussian() * 0.08));
        response.setWinRate(BigDecimal.valueOf(0.55 + random.nextGaussian() * 0.15));
    }
    
    private void calculateMaxDrawdown(List<PortfolioHistory> history, AnalyticsResponse response) {
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
            
            BigDecimal drawdown = peak.subtract(point.getNav()).divide(peak, 6, RoundingMode.HALF_UP);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
                maxDrawdownDuration = currentDrawdownDuration;
            }
        }
        
        response.setMaxDrawdown(maxDrawdown);
        response.setMaxDrawdownDuration(maxDrawdownDuration);
    }
    
    private void calculateBenchmarkMetrics(Portfolio portfolio, AnalyticsResponse response, AnalyticsRequest request, List<BigDecimal> returns) {
        try {
            // Get benchmark data (simplified for demo)
            Map<String, Object> benchmarkData = marketDataService.getMarketData(request.getBenchmarkSymbol(), "1d");
            
            // Simulate benchmark returns
            List<BigDecimal> benchmarkReturns = new ArrayList<>();
            for (int i = 0; i < returns.size(); i++) {
                benchmarkReturns.add(BigDecimal.valueOf(random.nextGaussian() * 0.01)); // 1% daily volatility
            }
            
            // Calculate beta
            BigDecimal covariance = calculateCovariance(returns, benchmarkReturns);
            BigDecimal benchmarkVariance = calculateVariance(benchmarkReturns);
            
            if (benchmarkVariance.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal beta = covariance.divide(benchmarkVariance, 6, RoundingMode.HALF_UP);
                response.setBeta(beta);
                
                // Calculate alpha
                BigDecimal avgPortfolioReturn = returns.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
                
                BigDecimal avgBenchmarkReturn = benchmarkReturns.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(benchmarkReturns.size()), 6, RoundingMode.HALF_UP);
                
                BigDecimal riskFreeRate = BigDecimal.valueOf(0.05).divide(BigDecimal.valueOf(252), 6, RoundingMode.HALF_UP);
                BigDecimal expectedReturn = riskFreeRate.add(beta.multiply(avgBenchmarkReturn.subtract(riskFreeRate)));
                BigDecimal alpha = avgPortfolioReturn.subtract(expectedReturn);
                
                response.setAlpha(alpha);
                response.setBenchmarkReturn(avgBenchmarkReturn);
                response.setOutperformance(avgPortfolioReturn.subtract(avgBenchmarkReturn));
            }
            
            // Calculate correlation
            BigDecimal correlation = calculateCorrelation(returns, benchmarkReturns);
            response.setCorrelationToBenchmark(correlation);
            
        } catch (Exception e) {
            logger.error("Failed to calculate benchmark metrics", e);
        }
    }
    
    private void calculateAttributionAnalysis(Portfolio portfolio, AnalyticsResponse response, AnalyticsRequest request) {
        // Simplified attribution analysis
        Map<String, BigDecimal> assetAttribution = new HashMap<>();
        assetAttribution.put("AAPL", BigDecimal.valueOf(0.45));
        assetAttribution.put("MSFT", BigDecimal.valueOf(0.30));
        assetAttribution.put("GOOGL", BigDecimal.valueOf(0.15));
        assetAttribution.put("Cash", BigDecimal.valueOf(0.10));
        
        Map<String, BigDecimal> sectorAttribution = new HashMap<>();
        sectorAttribution.put("Technology", BigDecimal.valueOf(0.75));
        sectorAttribution.put("Cash", BigDecimal.valueOf(0.25));
        
        response.setAssetAttribution(assetAttribution);
        response.setSectorAttribution(sectorAttribution);
    }
    
    private void calculateCorrelationMatrix(Portfolio portfolio, AnalyticsResponse response, AnalyticsRequest request) {
        // Simplified correlation matrix
        Map<String, Map<String, BigDecimal>> correlationMatrix = new HashMap<>();
        
        Map<String, BigDecimal> aaplCorr = new HashMap<>();
        aaplCorr.put("MSFT", BigDecimal.valueOf(0.65));
        aaplCorr.put("GOOGL", BigDecimal.valueOf(0.72));
        aaplCorr.put("TSLA", BigDecimal.valueOf(0.45));
        correlationMatrix.put("AAPL", aaplCorr);
        
        Map<String, BigDecimal> msftCorr = new HashMap<>();
        msftCorr.put("GOOGL", BigDecimal.valueOf(0.68));
        msftCorr.put("TSLA", BigDecimal.valueOf(0.38));
        correlationMatrix.put("MSFT", msftCorr);
        
        response.setCorrelationMatrix(correlationMatrix);
        response.setAvgCorrelation(BigDecimal.valueOf(0.55));
        response.setMaxCorrelation(BigDecimal.valueOf(0.72));
        response.setMinCorrelation(BigDecimal.valueOf(0.38));
        response.setDiversificationRatio(BigDecimal.valueOf(0.78));
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
        
        return covariance.divide(BigDecimal.valueOf(returns1.size()), 6, RoundingMode.HALF_UP);
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
                .divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        return variance;
    }
    
    private BigDecimal calculateCorrelation(List<BigDecimal> returns1, List<BigDecimal> returns2) {
        BigDecimal covariance = calculateCovariance(returns1, returns2);
        BigDecimal stdDev1 = BigDecimal.valueOf(Math.sqrt(calculateVariance(returns1).doubleValue()));
        BigDecimal stdDev2 = BigDecimal.valueOf(Math.sqrt(calculateVariance(returns2).doubleValue()));
        
        if (stdDev1.compareTo(BigDecimal.ZERO) > 0 && stdDev2.compareTo(BigDecimal.ZERO) > 0) {
            return covariance.divide(stdDev1.multiply(stdDev2), 6, RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }
    
    private void generateReportAsync(Report report, ReportRequest request) {
        // In a real implementation, this would be executed in a separate thread or job queue
        new Thread(() -> {
            try {
                report.setStatus(ReportStatus.GENERATING);
                reportRepository.save(report);
                
                // Simulate report generation
                Thread.sleep(2000); // Simulate processing time
                
                // Generate report content based on type
                String reportContent = generateReportContent(report, request);
                
                // In a real implementation, you would save the file to storage
                report.setFilePath("/reports/" + report.getId() + "." + report.getFileFormat().name().toLowerCase());
                report.setFileSize(reportContent.length());
                report.setStatus(ReportStatus.COMPLETED);
                report.setGeneratedAt(LocalDateTime.now());
                
                reportRepository.save(report);
                
            } catch (Exception e) {
                logger.error("Report generation failed for report {}", report.getId(), e);
                report.setStatus(ReportStatus.FAILED);
                report.setErrorMessage(e.getMessage());
                reportRepository.save(report);
            }
        }).start();
    }
    
    private String generateReportContent(Report report, ReportRequest request) {
        // Simplified report generation
        StringBuilder content = new StringBuilder();
        content.append("QuantCrux ").append(report.getReportType().getDisplayName()).append("\n");
        content.append("Generated: ").append(LocalDateTime.now()).append("\n");
        content.append("Portfolio: ").append(report.getPortfolio() != null ? report.getPortfolio().getName() : "N/A").append("\n");
        content.append("Period: ").append(report.getPeriodStart()).append(" to ").append(report.getPeriodEnd()).append("\n");
        content.append("\n--- Report Content ---\n");
        content.append("This is a simulated report. In a real implementation, this would contain detailed analytics, charts, and formatted data.");
        
        return content.toString();
    }
    
    private ReportResponse convertReportToResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setPortfolioId(report.getPortfolio() != null ? report.getPortfolio().getId() : null);
        response.setStrategyId(report.getStrategy() != null ? report.getStrategy().getId() : null);
        response.setPortfolioName(report.getPortfolio() != null ? report.getPortfolio().getName() : null);
        response.setStrategyName(report.getStrategy() != null ? report.getStrategy().getName() : null);
        response.setReportType(report.getReportType());
        response.setReportName(report.getReportName());
        response.setDescription(report.getDescription());
        response.setPeriodStart(report.getPeriodStart());
        response.setPeriodEnd(report.getPeriodEnd());
        response.setFileFormat(report.getFileFormat());
        response.setFilePath(report.getFilePath());
        response.setFileSize(report.getFileSize());
        response.setStatus(report.getStatus());
        response.setErrorMessage(report.getErrorMessage());
        response.setUserName(report.getUser().getFullName());
        response.setCreatedAt(report.getCreatedAt());
        response.setGeneratedAt(report.getGeneratedAt());
        response.setDownloadedAt(report.getDownloadedAt());
        response.setExpiresAt(report.getExpiresAt());
        
        // Set calculated fields
        if (report.getFilePath() != null) {
            response.setDownloadUrl("/api/reports/" + report.getId() + "/download");
        }
        
        response.setIsExpired(report.getExpiresAt() != null && report.getExpiresAt().isBefore(LocalDateTime.now()));
        
        if (report.getFileSize() != null) {
            response.setFileSizeFormatted(formatFileSize(report.getFileSize()));
        }
        
        return response;
    }
    
    private String formatFileSize(Integer bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
}