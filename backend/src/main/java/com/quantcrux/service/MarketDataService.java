package com.quantcrux.service;

import com.quantcrux.dto.*;
import com.quantcrux.model.*;
import com.quantcrux.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MarketDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);
    
    @Autowired
    private MarketDataCacheRepository cacheRepository;
    
    @Autowired
    private SymbolMetadataRepository symbolRepository;
    
    @Autowired
    private DataSourceRepository dataSourceRepository;
    
    @Autowired
    private BenchmarkDataRepository benchmarkRepository;
    
    private final Random random = new Random();
    
    public MarketDataResponse getLivePrice(String symbol) {
        return getMarketData(new MarketDataRequest(symbol, DataType.LIVE_PRICE));
    }
    
    public MarketDataResponse getOHLCVData(String symbol, String timeframe, LocalDateTime startTime, LocalDateTime endTime) {
        MarketDataRequest request = new MarketDataRequest(symbol, DataType.OHLCV);
        request.setTimeframe(timeframe);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        return getMarketData(request);
    }
    
    public MarketDataResponse getMarketData(MarketDataRequest request) {
        try {
            // Check cache first (unless force refresh)
            if (!request.getForceRefresh()) {
                Optional<MarketDataCache> cached = getCachedData(request);
                if (cached.isPresent()) {
                    return convertCacheToResponse(cached.get());
                }
            }
            
            // Fetch from external source
            MarketDataResponse response = fetchFromExternalSource(request);
            
            // Cache the result
            cacheMarketData(request, response);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to get market data for symbol: {}", request.getSymbol(), e);
            
            // Try to return stale cached data as fallback
            Optional<MarketDataCache> staleData = getStaleData(request);
            if (staleData.isPresent()) {
                MarketDataResponse response = convertCacheToResponse(staleData.get());
                response.setIsStale(true);
                response.setMessage("Using stale data due to API error: " + e.getMessage());
                return response;
            }
            
            // Generate mock data as last resort
            return generateMockData(request);
        }
    }
    
    public List<SymbolSearchResponse> searchSymbols(String query) {
        List<SymbolMetadata> symbols = symbolRepository.searchBySymbolOrName(query);
        
        return symbols.stream()
                .limit(20) // Limit results
                .map(this::convertSymbolToSearchResponse)
                .collect(Collectors.toList());
    }
    
    public List<SymbolSearchResponse> getPopularSymbols(AssetType assetType) {
        List<SymbolMetadata> symbols;
        
        if (assetType != null) {
            symbols = symbolRepository.findByAssetTypeAndTradeableOrderByMarketCap(assetType);
        } else {
            symbols = symbolRepository.findTradeableOrderByMarketCap();
        }
        
        return symbols.stream()
                .limit(50) // Top 50
                .map(this::convertSymbolToSearchResponse)
                .collect(Collectors.toList());
    }
    
    public List<MarketDataResponse> getBenchmarkData(String symbol, LocalDateTime startTime, LocalDateTime endTime) {
        List<BenchmarkData> benchmarkData = benchmarkRepository.findBySymbolAndDateBetween(
            symbol, startTime.toLocalDate(), endTime.toLocalDate());
        
        return benchmarkData.stream()
                .map(this::convertBenchmarkToResponse)
                .collect(Collectors.toList());
    }
    
    public void refreshCache() {
        // Clean expired entries
        int deletedCount = cacheRepository.deleteExpiredEntries();
        logger.info("Cleaned {} expired cache entries", deletedCount);
        
        // Refresh popular symbols
        List<String> popularSymbols = Arrays.asList("AAPL", "MSFT", "GOOGL", "TSLA", "BTCUSD", "ETHUSD", "SPY", "QQQ");
        
        for (String symbol : popularSymbols) {
            try {
                MarketDataRequest request = new MarketDataRequest(symbol, DataType.LIVE_PRICE);
                request.setForceRefresh(true);
                getMarketData(request);
            } catch (Exception e) {
                logger.warn("Failed to refresh cache for symbol: {}", symbol, e);
            }
        }
    }
    
    private Optional<MarketDataCache> getCachedData(MarketDataRequest request) {
        if (request.getTimeframe() != null) {
            return cacheRepository.findBySymbolAndDataTypeAndTimeframe(
                request.getSymbol(), request.getDataType(), request.getTimeframe());
        } else {
            return cacheRepository.findLatestValidBySymbolAndDataType(
                request.getSymbol(), request.getDataType());
        }
    }
    
    private Optional<MarketDataCache> getStaleData(MarketDataRequest request) {
        // Look for any cached data, even if expired
        if (request.getTimeframe() != null) {
            return cacheRepository.findBySymbolAndDataTypeAndTimeframe(
                request.getSymbol(), request.getDataType(), request.getTimeframe());
        } else {
            return cacheRepository.findBySymbolAndDataType(
                request.getSymbol(), request.getDataType());
        }
    }
    
    private MarketDataResponse fetchFromExternalSource(MarketDataRequest request) {
        // Get available data sources
        List<DataSource> sources = getAvailableDataSources(request);
        
        if (sources.isEmpty()) {
            throw new RuntimeException("No available data sources for request");
        }
        
        // Try each source in priority order
        for (DataSource source : sources) {
            try {
                return fetchFromSource(source, request);
            } catch (Exception e) {
                logger.warn("Failed to fetch from source {}: {}", source.getName(), e.getMessage());
                logSourceFailure(source, e.getMessage());
            }
        }
        
        throw new RuntimeException("All data sources failed");
    }
    
    private List<DataSource> getAvailableDataSources(MarketDataRequest request) {
        List<DataSource> sources = dataSourceRepository.findSourcesWithinRateLimit();
        
        // Filter by capability
        return sources.stream()
                .filter(source -> {
                    if (request.getDataType() == DataType.LIVE_PRICE && !source.getSupportsLivePrices()) {
                        return false;
                    }
                    if (request.getDataType() == DataType.OHLCV && !source.getSupportsHistorical()) {
                        return false;
                    }
                    
                    // Check if symbol is crypto and source supports it
                    Optional<SymbolMetadata> symbolMeta = symbolRepository.findBySymbol(request.getSymbol());
                    if (symbolMeta.isPresent() && symbolMeta.get().getAssetType() == AssetType.CRYPTO) {
                        return source.getSupportsCrypto();
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    private MarketDataResponse fetchFromSource(DataSource source, MarketDataRequest request) {
        // Simulate external API call
        // In a real implementation, this would make HTTP requests to external APIs
        
        updateSourceUsage(source);
        
        MarketDataResponse response = new MarketDataResponse();
        response.setSymbol(request.getSymbol());
        response.setDataType(request.getDataType());
        response.setSource(source.getName());
        response.setDataTimestamp(LocalDateTime.now());
        response.setQualityScore(100);
        
        if (request.getDataType() == DataType.LIVE_PRICE) {
            // Generate realistic price data
            BigDecimal basePrice = getBasePrice(request.getSymbol());
            BigDecimal volatility = getSymbolVolatility(request.getSymbol());
            
            BigDecimal change = BigDecimal.valueOf(random.nextGaussian() * volatility.doubleValue());
            BigDecimal currentPrice = basePrice.multiply(BigDecimal.ONE.add(change))
                    .setScale(6, RoundingMode.HALF_UP);
            
            response.setPrice(currentPrice);
            response.setBidPrice(currentPrice.multiply(BigDecimal.valueOf(0.9995)));
            response.setAskPrice(currentPrice.multiply(BigDecimal.valueOf(1.0005)));
            response.setDayChange(currentPrice.subtract(basePrice));
            response.setDayChangePercent(change);
            response.setVolume(BigDecimal.valueOf(100000 + random.nextInt(900000)));
            
        } else if (request.getDataType() == DataType.OHLCV) {
            // Generate OHLCV data
            response.setTimeframe(request.getTimeframe());
            response.setOhlcvData(generateOHLCVData(request));
        }
        
        return response;
    }
    
    private List<MarketDataResponse.OHLCVData> generateOHLCVData(MarketDataRequest request) {
        List<MarketDataResponse.OHLCVData> data = new ArrayList<>();
        BigDecimal basePrice = getBasePrice(request.getSymbol());
        BigDecimal currentPrice = basePrice;
        
        LocalDateTime start = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now().minusDays(30);
        LocalDateTime end = request.getEndTime() != null ? request.getEndTime() : LocalDateTime.now();
        
        LocalDateTime current = start;
        while (current.isBefore(end) && data.size() < request.getLimit()) {
            BigDecimal open = currentPrice;
            BigDecimal change = BigDecimal.valueOf(random.nextGaussian() * 0.02); // 2% volatility
            BigDecimal close = open.multiply(BigDecimal.ONE.add(change));
            
            BigDecimal high = open.max(close).multiply(BigDecimal.valueOf(1 + random.nextDouble() * 0.01));
            BigDecimal low = open.min(close).multiply(BigDecimal.valueOf(1 - random.nextDouble() * 0.01));
            BigDecimal volume = BigDecimal.valueOf(100000 + random.nextInt(900000));
            
            MarketDataResponse.OHLCVData ohlcv = new MarketDataResponse.OHLCVData(
                current, open, high, low, close, volume);
            data.add(ohlcv);
            
            currentPrice = close;
            current = getNextTimeframe(current, request.getTimeframe());
        }
        
        return data;
    }
    
    private LocalDateTime getNextTimeframe(LocalDateTime current, String timeframe) {
        switch (timeframe) {
            case "1m": return current.plusMinutes(1);
            case "5m": return current.plusMinutes(5);
            case "15m": return current.plusMinutes(15);
            case "30m": return current.plusMinutes(30);
            case "1h": return current.plusHours(1);
            case "4h": return current.plusHours(4);
            case "1d": return current.plusDays(1);
            default: return current.plusHours(1);
        }
    }
    
    private void cacheMarketData(MarketDataRequest request, MarketDataResponse response) {
        try {
            MarketDataCache cache = new MarketDataCache();
            cache.setSymbol(request.getSymbol());
            cache.setDataType(request.getDataType());
            cache.setTimeframe(request.getTimeframe());
            cache.setPrice(response.getPrice());
            cache.setBidPrice(response.getBidPrice());
            cache.setAskPrice(response.getAskPrice());
            cache.setDayChange(response.getDayChange());
            cache.setDayChangePercent(response.getDayChangePercent());
            cache.setVolume(response.getVolume());
            cache.setDataTimestamp(response.getDataTimestamp());
            cache.setSource(response.getSource());
            cache.setQualityScore(response.getQualityScore());
            
            // Set expiry based on data type
            LocalDateTime expiry = LocalDateTime.now();
            if (request.getDataType() == DataType.LIVE_PRICE) {
                expiry = expiry.plusMinutes(1); // Live prices expire in 1 minute
            } else {
                expiry = expiry.plusHours(1); // Historical data expires in 1 hour
            }
            cache.setExpiresAt(expiry);
            
            cacheRepository.save(cache);
            
        } catch (Exception e) {
            logger.error("Failed to cache market data for symbol: {}", request.getSymbol(), e);
        }
    }
    
    private void updateSourceUsage(DataSource source) {
        source.setLastRequestAt(LocalDateTime.now());
        source.setRequestsToday(source.getRequestsToday() + 1);
        source.setRequestsThisMinute(source.getRequestsThisMinute() + 1);
        source.setConsecutiveFailures(0);
        dataSourceRepository.save(source);
    }
    
    private void logSourceFailure(DataSource source, String errorMessage) {
        source.setConsecutiveFailures(source.getConsecutiveFailures() + 1);
        source.setLastErrorMessage(errorMessage);
        source.setLastErrorAt(LocalDateTime.now());
        dataSourceRepository.save(source);
    }
    
    private BigDecimal getBasePrice(String symbol) {
        switch (symbol.toUpperCase()) {
            case "AAPL": return BigDecimal.valueOf(172.50);
            case "MSFT": return BigDecimal.valueOf(415.75);
            case "GOOGL": return BigDecimal.valueOf(175.85);
            case "TSLA": return BigDecimal.valueOf(248.50);
            case "AMZN": return BigDecimal.valueOf(185.90);
            case "NVDA": return BigDecimal.valueOf(875.25);
            case "META": return BigDecimal.valueOf(485.60);
            case "NFLX": return BigDecimal.valueOf(485.30);
            case "BTCUSD": return BigDecimal.valueOf(97250.00);
            case "ETHUSD": return BigDecimal.valueOf(3420.50);
            case "ADAUSD": return BigDecimal.valueOf(1.25);
            case "SOLUSD": return BigDecimal.valueOf(185.75);
            case "SPY": return BigDecimal.valueOf(483.61);
            case "QQQ": return BigDecimal.valueOf(425.80);
            case "VTI": return BigDecimal.valueOf(285.45);
            default: return BigDecimal.valueOf(100.00);
        }
    }
    
    private BigDecimal getSymbolVolatility(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTCUSD":
            case "ETHUSD":
            case "ADAUSD":
            case "SOLUSD":
                return BigDecimal.valueOf(0.04); // 4% daily volatility for crypto
            case "TSLA":
                return BigDecimal.valueOf(0.03); // 3% for volatile stocks
            case "SPY":
            case "QQQ":
            case "VTI":
                return BigDecimal.valueOf(0.01); // 1% for ETFs
            default:
                return BigDecimal.valueOf(0.02); // 2% for regular stocks
        }
    }
    
    private MarketDataResponse generateMockData(MarketDataRequest request) {
        logger.warn("Generating mock data for symbol: {}", request.getSymbol());
        
        MarketDataResponse response = new MarketDataResponse();
        response.setSymbol(request.getSymbol());
        response.setDataType(request.getDataType());
        response.setSource("mock");
        response.setDataTimestamp(LocalDateTime.now());
        response.setQualityScore(50); // Lower quality for mock data
        response.setMessage("Mock data - external APIs unavailable");
        
        if (request.getDataType() == DataType.LIVE_PRICE) {
            BigDecimal basePrice = getBasePrice(request.getSymbol());
            response.setPrice(basePrice);
            response.setBidPrice(basePrice.multiply(BigDecimal.valueOf(0.999)));
            response.setAskPrice(basePrice.multiply(BigDecimal.valueOf(1.001)));
            response.setDayChange(BigDecimal.valueOf(random.nextGaussian() * 5));
            response.setDayChangePercent(BigDecimal.valueOf(random.nextGaussian() * 0.02));
            response.setVolume(BigDecimal.valueOf(100000 + random.nextInt(500000)));
        }
        
        return response;
    }
    
    private MarketDataResponse convertCacheToResponse(MarketDataCache cache) {
        MarketDataResponse response = new MarketDataResponse();
        response.setSymbol(cache.getSymbol());
        response.setDataType(cache.getDataType());
        response.setTimeframe(cache.getTimeframe());
        response.setPrice(cache.getPrice());
        response.setBidPrice(cache.getBidPrice());
        response.setAskPrice(cache.getAskPrice());
        response.setDayChange(cache.getDayChange());
        response.setDayChangePercent(cache.getDayChangePercent());
        response.setVolume(cache.getVolume());
        response.setDataTimestamp(cache.getDataTimestamp());
        response.setSource(cache.getSource());
        response.setQualityScore(cache.getQualityScore());
        response.setIsStale(cache.getExpiresAt().isBefore(LocalDateTime.now()));
        
        return response;
    }
    
    private SymbolSearchResponse convertSymbolToSearchResponse(SymbolMetadata symbol) {
        SymbolSearchResponse response = new SymbolSearchResponse();
        response.setSymbol(symbol.getSymbol());
        response.setName(symbol.getName());
        response.setExchange(symbol.getExchange());
        response.setCurrency(symbol.getCurrency());
        response.setAssetType(symbol.getAssetType());
        response.setSector(symbol.getSector());
        response.setIndustry(symbol.getIndustry());
        response.setCountry(symbol.getCountry());
        response.setIsTradeable(symbol.getIsTradeable());
        response.setMarketCap(symbol.getMarketCap());
        response.setDescription(symbol.getDescription());
        
        return response;
    }
    
    private MarketDataResponse convertBenchmarkToResponse(BenchmarkData benchmark) {
        MarketDataResponse response = new MarketDataResponse();
        response.setSymbol(benchmark.getSymbol());
        response.setDataType(DataType.OHLCV);
        response.setPrice(benchmark.getClosePrice());
        response.setDataTimestamp(benchmark.getDate().atStartOfDay());
        response.setSource("benchmark");
        response.setQualityScore(100);
        
        return response;
    }
    
    // Legacy method for backward compatibility
    public Map<String, Object> getMarketData(String symbol, String timeframe) {
        MarketDataResponse response = getLivePrice(symbol);
        
        Map<String, Object> data = new HashMap<>();
        data.put("symbol", response.getSymbol());
        data.put("price", response.getPrice());
        data.put("bid", response.getBidPrice());
        data.put("ask", response.getAskPrice());
        data.put("volume", response.getVolume());
        data.put("dayChange", response.getDayChange());
        data.put("dayChangePercent", response.getDayChangePercent());
        data.put("timestamp", response.getDataTimestamp());
        data.put("source", response.getSource());
        
        return data;
    }
}