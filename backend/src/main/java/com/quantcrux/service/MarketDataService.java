package com.quantcrux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantcrux.dto.*;
import com.quantcrux.model.*;
import com.quantcrux.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    
    @Value("${finnhub.api.key:}")
    private String finnhubApiKey;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();
    
    public MarketDataService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }
    
    public MarketDataResponse getLivePrice(String symbol) {
        MarketDataRequest request = new MarketDataRequest(symbol, DataType.LIVE_PRICE);
        return getMarketData(request);
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
                    logger.debug("Returning cached data for symbol: {}", request.getSymbol());
                    return convertCacheToResponse(cached.get());
                }
            }
            
            // Fetch from external source
            logger.info("Fetching live data for symbol: {} from external sources", request.getSymbol());
            MarketDataResponse response = fetchFromExternalSource(request);
            
            // Cache the result (only for live prices due to schema limitations)
            if (request.getDataType() == DataType.LIVE_PRICE) {
                cacheMarketData(request, response);
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to get market data for symbol: {}", request.getSymbol(), e);
            
            // Try to return stale cached data as fallback
            Optional<MarketDataCache> staleData = getStaleData(request);
            if (staleData.isPresent()) {
                logger.warn("Using stale cached data for symbol: {}", request.getSymbol());
                MarketDataResponse response = convertCacheToResponse(staleData.get());
                response.setIsStale(true);
                response.setMessage("Using stale data due to API error: " + e.getMessage());
                return response;
            }
            
            // Generate mock data as last resort
            logger.warn("Generating mock data for symbol: {} due to API failure", request.getSymbol());
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
                Thread.sleep(100); // Rate limiting
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
            logger.warn("No available data sources for symbol: {}", request.getSymbol());
            return generateMockData(request);
        }
        
        // Try each source in priority order
        for (DataSource source : sources) {
            try {
                logger.debug("Attempting to fetch from source: {} for symbol: {}", source.getName(), request.getSymbol());
                return fetchFromSource(source, request);
            } catch (Exception e) {
                logger.warn("Failed to fetch from source {}: {}", source.getName(), e.getMessage());
                logSourceFailure(source, e.getMessage());
            }
        }
        
        logger.error("All data sources failed for symbol: {}", request.getSymbol());
        return generateMockData(request);
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
    
    // private MarketDataResponse fetchFromSource(DataSource source, MarketDataRequest request) {
    //     updateSourceUsage(source);
        
    //     try {
    //         String apiUrl = buildApiUrl(source, request);
    //         logger.debug("Fetching from URL: {}", apiUrl);
            
    //         String apiResponseJson = webClient.get()
    //                 .uri(apiUrl)
    //                 .retrieve()
    //                 .bodyToMono(String.class)
    //                 .timeout(java.time.Duration.ofSeconds(10))
    //                 .onErrorResume(WebClientResponseException.class, ex -> {
    //                     logger.error("API call failed with status: {} for URL: {}", ex.getStatusCode(), apiUrl);
    //                     return Mono.error(new RuntimeException("API call failed: " + ex.getMessage()));
    //                 })
    //                 .block();
            
    //         if (apiResponseJson == null || apiResponseJson.trim().isEmpty()) {
    //             throw new RuntimeException("Empty response from API");
    //         }
            
    //         return parseApiResponse(apiResponseJson, request, source);
            
    //     } catch (Exception e) {
    //         logger.error("Failed to fetch from source {}: {}", source.getName(), e.getMessage());
    //         logSourceFailure(source, e.getMessage());
    //         throw e;
    //     }
    // }

    private MarketDataResponse fetchFromSource(DataSource source, MarketDataRequest request) {
        updateSourceUsage(source);
        
        try {
            String apiUrl = buildApiUrl(source, request);
            logger.debug("Fetching from URL: {}", apiUrl);
            
            String apiResponseJson = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        logger.error("API call failed with status: {} for URL: {}", ex.getStatusCode(), apiUrl);
                        return Mono.error(new RuntimeException("API call failed: " + ex.getMessage()));
                    })
                    .block();
            
            if (apiResponseJson == null || apiResponseJson.trim().isEmpty()) {
                throw new RuntimeException("Empty response from API");
            }

            return parseApiResponse(apiResponseJson, request, source);
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse API response for source {}: {}", source.getName(), e.getMessage());
            logSourceFailure(source, "Parsing error: " + e.getMessage());
            throw new RuntimeException("Failed to parse API response", e);
        } catch (Exception e) {
            logger.error("Failed to fetch from source {}: {}", source.getName(), e.getMessage());
            logSourceFailure(source, e.getMessage());
            throw e;
        }
    }

    
    private String buildApiUrl(DataSource source, MarketDataRequest request) {
        String symbol = request.getSymbol().toUpperCase();
        
        switch (source.getName().toLowerCase()) {
            case "finnhub":
                return buildFinnhubUrl(request, symbol);
            case "twelvedata":
                return buildTwelveDataUrl(request, symbol);
            case "yfinance":
                return buildYFinanceUrl(request, symbol);
            case "coingecko":
                return buildCoinGeckoUrl(request, symbol);
            default:
                throw new RuntimeException("Unsupported data source: " + source.getName());
        }
    }
    
    private String buildFinnhubUrl(MarketDataRequest request, String symbol) {
        if (finnhubApiKey == null || finnhubApiKey.trim().isEmpty()) {
            throw new RuntimeException("Finnhub API key not configured");
        }
        
        switch (request.getDataType()) {
            case LIVE_PRICE:
                return String.format("https://finnhub.io/api/v1/quote?symbol=%s&token=%s", symbol, finnhubApiKey);
            case OHLCV:
                long startTimestamp = request.getStartTime() != null ? 
                    request.getStartTime().toEpochSecond(ZoneOffset.UTC) : 
                    LocalDateTime.now().minusDays(30).toEpochSecond(ZoneOffset.UTC);
                long endTimestamp = request.getEndTime() != null ? 
                    request.getEndTime().toEpochSecond(ZoneOffset.UTC) : 
                    LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                String resolution = mapTimeframeToFinnhubResolution(request.getTimeframe());
                return String.format("https://finnhub.io/api/v1/stock/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s", 
                    symbol, resolution, startTimestamp, endTimestamp, finnhubApiKey);
            default:
                throw new RuntimeException("Unsupported data type for Finnhub: " + request.getDataType());
        }
    }
    
    private String buildTwelveDataUrl(MarketDataRequest request, String symbol) {
        // TwelveData implementation (requires API key configuration)
        switch (request.getDataType()) {
            case LIVE_PRICE:
                return String.format("https://api.twelvedata.com/price?symbol=%s&apikey=demo", symbol);
            case OHLCV:
                String interval = mapTimeframeToTwelveDataInterval(request.getTimeframe());
                return String.format("https://api.twelvedata.com/time_series?symbol=%s&interval=%s&outputsize=30&apikey=demo", 
                    symbol, interval);
            default:
                throw new RuntimeException("Unsupported data type for TwelveData: " + request.getDataType());
        }
    }
    
    private String buildYFinanceUrl(MarketDataRequest request, String symbol) {
        // Yahoo Finance implementation (free but rate limited)
        switch (request.getDataType()) {
            case LIVE_PRICE:
                return String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", symbol);
            case OHLCV:
                long startTimestamp = request.getStartTime() != null ? 
                    request.getStartTime().toEpochSecond(ZoneOffset.UTC) : 
                    LocalDateTime.now().minusDays(30).toEpochSecond(ZoneOffset.UTC);
                long endTimestamp = request.getEndTime() != null ? 
                    request.getEndTime().toEpochSecond(ZoneOffset.UTC) : 
                    LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                String interval = mapTimeframeToYFinanceInterval(request.getTimeframe());
                return String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=%s", 
                    symbol, startTimestamp, endTimestamp, interval);
            default:
                throw new RuntimeException("Unsupported data type for Yahoo Finance: " + request.getDataType());
        }
    }
    
    private String buildCoinGeckoUrl(MarketDataRequest request, String symbol) {
        // CoinGecko implementation for crypto
        String coinId = mapSymbolToCoinGeckoId(symbol);
        
        switch (request.getDataType()) {
            case LIVE_PRICE:
                return String.format("https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd&include_24hr_change=true", coinId);
            case OHLCV:
                int days = request.getStartTime() != null && request.getEndTime() != null ?
                    (int) java.time.temporal.ChronoUnit.DAYS.between(request.getStartTime(), request.getEndTime()) : 30;
                return String.format("https://api.coingecko.com/api/v3/coins/%s/ohlc?vs_currency=usd&days=%d", coinId, days);
            default:
                throw new RuntimeException("Unsupported data type for CoinGecko: " + request.getDataType());
        }
    }
    
    private MarketDataResponse parseApiResponse(String apiResponseJson, MarketDataRequest request, DataSource source) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(apiResponseJson);
        
        switch (source.getName().toLowerCase()) {
            case "finnhub":
                return parseFinnhubResponse(rootNode, request, source);
            case "twelvedata":
                return parseTwelveDataResponse(rootNode, request, source);
            case "yfinance":
                return parseYFinanceResponse(rootNode, request, source);
            case "coingecko":
                return parseCoinGeckoResponse(rootNode, request, source);
            default:
                throw new RuntimeException("Unsupported data source for parsing: " + source.getName());
        }
    }
    
    private MarketDataResponse parseFinnhubResponse(JsonNode rootNode, MarketDataRequest request, DataSource source) {
        MarketDataResponse response = new MarketDataResponse();
        response.setSymbol(request.getSymbol());
        response.setDataType(request.getDataType());
        response.setSource(source.getName());
        response.setDataTimestamp(LocalDateTime.now());
        response.setQualityScore(100);
        
        if (request.getDataType() == DataType.LIVE_PRICE) {
            // Finnhub quote response: {"c":172.5,"d":2.15,"dp":1.26,"h":173.0,"l":170.5,"o":171.0,"pc":170.35,"t":1641234567}
            if (rootNode.has("c") && !rootNode.get("c").isNull()) {
                BigDecimal currentPrice = BigDecimal.valueOf(rootNode.get("c").asDouble());
                BigDecimal prevClose = rootNode.has("pc") ? BigDecimal.valueOf(rootNode.get("pc").asDouble()) : currentPrice;
                BigDecimal dayChange = rootNode.has("d") ? BigDecimal.valueOf(rootNode.get("d").asDouble()) : BigDecimal.ZERO;
                BigDecimal dayChangePercent = rootNode.has("dp") ? 
                    BigDecimal.valueOf(rootNode.get("dp").asDouble() / 100.0) : BigDecimal.ZERO;
                
                response.setPrice(currentPrice);
                response.setBidPrice(currentPrice.multiply(BigDecimal.valueOf(0.9995))); // Approximate bid
                response.setAskPrice(currentPrice.multiply(BigDecimal.valueOf(1.0005))); // Approximate ask
                response.setDayChange(dayChange);
                response.setDayChangePercent(dayChangePercent);
                
                // Estimate volume (Finnhub quote doesn't include volume)
                response.setVolume(BigDecimal.valueOf(100000 + random.nextInt(900000)));
            } else {
                throw new RuntimeException("Invalid Finnhub quote response: missing current price");
            }
            
        } else if (request.getDataType() == DataType.OHLCV) {
            // Finnhub candle response: {"c":[172.5,173.2],"h":[173.0,174.1],"l":[170.5,171.8],"o":[171.0,172.5],"s":"ok","t":[1641234567,1641320967],"v":[1000000,1200000]}
            if (rootNode.has("s") && "ok".equals(rootNode.get("s").asText())) {
                List<MarketDataResponse.OHLCVData> ohlcvData = new ArrayList<>();
                
                JsonNode closes = rootNode.get("c");
                JsonNode highs = rootNode.get("h");
                JsonNode lows = rootNode.get("l");
                JsonNode opens = rootNode.get("o");
                JsonNode timestamps = rootNode.get("t");
                JsonNode volumes = rootNode.get("v");
                
                if (closes != null && closes.isArray()) {
                    for (int i = 0; i < closes.size(); i++) {
                        LocalDateTime timestamp = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(timestamps.get(i).asLong()), ZoneOffset.UTC);
                        
                        MarketDataResponse.OHLCVData ohlcv = new MarketDataResponse.OHLCVData(
                            timestamp,
                            BigDecimal.valueOf(opens.get(i).asDouble()),
                            BigDecimal.valueOf(highs.get(i).asDouble()),
                            BigDecimal.valueOf(lows.get(i).asDouble()),
                            BigDecimal.valueOf(closes.get(i).asDouble()),
                            BigDecimal.valueOf(volumes.get(i).asLong())
                        );
                        ohlcvData.add(ohlcv);
                    }
                }
                
                response.setOhlcvData(ohlcvData);
                response.setTimeframe(request.getTimeframe());
            } else {
                throw new RuntimeException("Invalid Finnhub candle response or no data available");
            }
        }
        
        return response;
    }
    
    private MarketDataResponse parseTwelveDataResponse(JsonNode rootNode, MarketDataRequest request, DataSource source) {
        MarketDataResponse response = new MarketDataResponse();
        response.setSymbol(request.getSymbol());
        response.setDataType(request.getDataType());
        response.setSource(source.getName());
        response.setDataTimestamp(LocalDateTime.now());
        response.setQualityScore(95);
        
        if (request.getDataType() == DataType.LIVE_PRICE) {
            // TwelveData price response: {"price":"172.50"}
            if (rootNode.has("price")) {
                BigDecimal currentPrice = BigDecimal.valueOf(rootNode.get("price").asDouble());
                response.setPrice(currentPrice);
                response.setBidPrice(currentPrice.multiply(BigDecimal.valueOf(0.9995)));
                response.setAskPrice(currentPrice.multiply(BigDecimal.valueOf(1.0005)));
                response.setVolume(BigDecimal.valueOf(100000 + random.nextInt(900000)));
            } else {
                throw new RuntimeException("Invalid TwelveData price response");
            }
            
        } else if (request.getDataType() == DataType.OHLCV) {
            // TwelveData time series response
            if (rootNode.has("values") && rootNode.get("values").isArray()) {
                List<MarketDataResponse.OHLCVData> ohlcvData = new ArrayList<>();
                
                for (JsonNode value : rootNode.get("values")) {
                    LocalDateTime timestamp = LocalDateTime.parse(value.get("datetime").asText(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    
                    MarketDataResponse.OHLCVData ohlcv = new MarketDataResponse.OHLCVData(
                        timestamp,
                        BigDecimal.valueOf(value.get("open").asDouble()),
                        BigDecimal.valueOf(value.get("high").asDouble()),
                        BigDecimal.valueOf(value.get("low").asDouble()),
                        BigDecimal.valueOf(value.get("close").asDouble()),
                        BigDecimal.valueOf(value.get("volume").asLong())
                    );
                    ohlcvData.add(ohlcv);
                }
                
                response.setOhlcvData(ohlcvData);
                response.setTimeframe(request.getTimeframe());
            } else {
                throw new RuntimeException("Invalid TwelveData time series response");
            }
        }
        
        return response;
    }
    
    private MarketDataResponse parseYFinanceResponse(JsonNode rootNode, MarketDataRequest request, DataSource source) {
        MarketDataResponse response = new MarketDataResponse();
        response.setSymbol(request.getSymbol());
        response.setDataType(request.getDataType());
        response.setSource(source.getName());
        response.setDataTimestamp(LocalDateTime.now());
        response.setQualityScore(90);
        
        // Yahoo Finance response structure: {"chart":{"result":[{"meta":{},"timestamp":[],"indicators":{"quote":[{"open":[],"high":[],"low":[],"close":[],"volume":[]}]}}]}}
        JsonNode chart = rootNode.get("chart");
        if (chart == null || !chart.has("result") || chart.get("result").size() == 0) {
            throw new RuntimeException("Invalid Yahoo Finance response structure");
        }
        
        JsonNode result = chart.get("result").get(0);
        JsonNode meta = result.get("meta");
        
        if (request.getDataType() == DataType.LIVE_PRICE) {
            if (meta.has("regularMarketPrice")) {
                BigDecimal currentPrice = BigDecimal.valueOf(meta.get("regularMarketPrice").asDouble());
                BigDecimal prevClose = meta.has("previousClose") ? 
                    BigDecimal.valueOf(meta.get("previousClose").asDouble()) : currentPrice;
                
                response.setPrice(currentPrice);
                response.setBidPrice(currentPrice.multiply(BigDecimal.valueOf(0.9995)));
                response.setAskPrice(currentPrice.multiply(BigDecimal.valueOf(1.0005)));
                
                if (prevClose.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal dayChange = currentPrice.subtract(prevClose);
                    BigDecimal dayChangePercent = dayChange.divide(prevClose, 6, RoundingMode.HALF_UP);
                    response.setDayChange(dayChange);
                    response.setDayChangePercent(dayChangePercent);
                }
            } else {
                throw new RuntimeException("Invalid Yahoo Finance quote response");
            }
            
        } else if (request.getDataType() == DataType.OHLCV) {
            JsonNode timestamps = result.get("timestamp");
            JsonNode indicators = result.get("indicators");
            
            if (timestamps != null && indicators != null && indicators.has("quote")) {
                JsonNode quote = indicators.get("quote").get(0);
                List<MarketDataResponse.OHLCVData> ohlcvData = new ArrayList<>();
                
                for (int i = 0; i < timestamps.size(); i++) {
                    LocalDateTime timestamp = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamps.get(i).asLong()), ZoneOffset.UTC);
                    
                    // Skip null values
                    if (quote.get("close").get(i).isNull()) continue;
                    
                    MarketDataResponse.OHLCVData ohlcv = new MarketDataResponse.OHLCVData(
                        timestamp,
                        BigDecimal.valueOf(quote.get("open").get(i).asDouble()),
                        BigDecimal.valueOf(quote.get("high").get(i).asDouble()),
                        BigDecimal.valueOf(quote.get("low").get(i).asDouble()),
                        BigDecimal.valueOf(quote.get("close").get(i).asDouble()),
                        BigDecimal.valueOf(quote.get("volume").get(i).asLong())
                    );
                    ohlcvData.add(ohlcv);
                }
                
                response.setOhlcvData(ohlcvData);
                response.setTimeframe(request.getTimeframe());
            } else {
                throw new RuntimeException("Invalid Yahoo Finance OHLCV response");
            }
        }
        
        return response;
    }
    
    private MarketDataResponse parseCoinGeckoResponse(JsonNode rootNode, MarketDataRequest request, DataSource source) {
        MarketDataResponse response = new MarketDataResponse();
        response.setSymbol(request.getSymbol());
        response.setDataType(request.getDataType());
        response.setSource(source.getName());
        response.setDataTimestamp(LocalDateTime.now());
        response.setQualityScore(95);
        
        if (request.getDataType() == DataType.LIVE_PRICE) {
            // CoinGecko price response: {"bitcoin":{"usd":45000,"usd_24h_change":2.5}}
            String coinId = mapSymbolToCoinGeckoId(request.getSymbol());
            
            if (rootNode.has(coinId)) {
                JsonNode coinData = rootNode.get(coinId);
                if (coinData.has("usd")) {
                    BigDecimal currentPrice = BigDecimal.valueOf(coinData.get("usd").asDouble());
                    BigDecimal dayChangePercent = coinData.has("usd_24h_change") ? 
                        BigDecimal.valueOf(coinData.get("usd_24h_change").asDouble() / 100.0) : BigDecimal.ZERO;
                    
                    response.setPrice(currentPrice);
                    response.setBidPrice(currentPrice.multiply(BigDecimal.valueOf(0.999))); // Wider spread for crypto
                    response.setAskPrice(currentPrice.multiply(BigDecimal.valueOf(1.001)));
                    response.setDayChangePercent(dayChangePercent);
                    response.setDayChange(currentPrice.multiply(dayChangePercent));
                } else {
                    throw new RuntimeException("Invalid CoinGecko price response");
                }
            } else {
                throw new RuntimeException("Coin not found in CoinGecko response");
            }
            
        } else if (request.getDataType() == DataType.OHLCV) {
            // CoinGecko OHLC response: [[timestamp,open,high,low,close],...]
            if (rootNode.isArray()) {
                List<MarketDataResponse.OHLCVData> ohlcvData = new ArrayList<>();
                
                for (JsonNode candle : rootNode) {
                    if (candle.isArray() && candle.size() >= 5) {
                        LocalDateTime timestamp = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(candle.get(0).asLong()), ZoneOffset.UTC);
                        
                        MarketDataResponse.OHLCVData ohlcv = new MarketDataResponse.OHLCVData(
                            timestamp,
                            BigDecimal.valueOf(candle.get(1).asDouble()),
                            BigDecimal.valueOf(candle.get(2).asDouble()),
                            BigDecimal.valueOf(candle.get(3).asDouble()),
                            BigDecimal.valueOf(candle.get(4).asDouble()),
                            BigDecimal.valueOf(1000000) // CoinGecko OHLC doesn't include volume
                        );
                        ohlcvData.add(ohlcv);
                    }
                }
                
                response.setOhlcvData(ohlcvData);
                response.setTimeframe(request.getTimeframe());
            } else {
                throw new RuntimeException("Invalid CoinGecko OHLC response format");
            }
        }
        
        return response;
    }
    
    private String mapTimeframeToFinnhubResolution(String timeframe) {
        if (timeframe == null) return "D";
        switch (timeframe.toLowerCase()) {
            case "1m": return "1";
            case "5m": return "5";
            case "15m": return "15";
            case "30m": return "30";
            case "1h": return "60";
            case "4h": return "240";
            case "1d": return "D";
            case "1w": return "W";
            default: return "D";
        }
    }
    
    private String mapTimeframeToTwelveDataInterval(String timeframe) {
        if (timeframe == null) return "1day";
        switch (timeframe.toLowerCase()) {
            case "1m": return "1min";
            case "5m": return "5min";
            case "15m": return "15min";
            case "30m": return "30min";
            case "1h": return "1h";
            case "4h": return "4h";
            case "1d": return "1day";
            case "1w": return "1week";
            default: return "1day";
        }
    }
    
    private String mapTimeframeToYFinanceInterval(String timeframe) {
        if (timeframe == null) return "1d";
        switch (timeframe.toLowerCase()) {
            case "1m": return "1m";
            case "5m": return "5m";
            case "15m": return "15m";
            case "30m": return "30m";
            case "1h": return "1h";
            case "4h": return "4h";
            case "1d": return "1d";
            case "1w": return "1wk";
            default: return "1d";
        }
    }
    
    private String mapSymbolToCoinGeckoId(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTCUSD":
            case "BTC":
                return "bitcoin";
            case "ETHUSD":
            case "ETH":
                return "ethereum";
            case "ADAUSD":
            case "ADA":
                return "cardano";
            case "SOLUSD":
            case "SOL":
                return "solana";
            case "DOTUSD":
            case "DOT":
                return "polkadot";
            case "LINKUSD":
            case "LINK":
                return "chainlink";
            default:
                return symbol.toLowerCase().replace("usd", "");
        }
    }
    
    private void cacheMarketData(MarketDataRequest request, MarketDataResponse response) {
        try {
            // Only cache live prices due to current schema limitations
            if (request.getDataType() != DataType.LIVE_PRICE) {
                logger.debug("Skipping cache for data type: {} (not supported by current schema)", request.getDataType());
                return;
            }
            
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
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(1); // Live prices expire in 1 minute
            cache.setExpiresAt(expiry);
            
            cacheRepository.save(cache);
            logger.debug("Cached market data for symbol: {}", request.getSymbol());
            
        } catch (Exception e) {
            logger.error("Failed to cache market data for symbol: {}", request.getSymbol(), e);
        }
    }
    
    private void updateSourceUsage(DataSource source) {
        try {
            source.setLastRequestAt(LocalDateTime.now());
            source.setRequestsToday(source.getRequestsToday() + 1);
            source.setRequestsThisMinute(source.getRequestsThisMinute() + 1);
            source.setConsecutiveFailures(0);
            dataSourceRepository.save(source);
        } catch (Exception e) {
            logger.error("Failed to update source usage for: {}", source.getName(), e);
        }
    }
    
    private void logSourceFailure(DataSource source, String errorMessage) {
        try {
            source.setConsecutiveFailures(source.getConsecutiveFailures() + 1);
            source.setLastErrorMessage(errorMessage);
            source.setLastErrorAt(LocalDateTime.now());
            dataSourceRepository.save(source);
        } catch (Exception e) {
            logger.error("Failed to log source failure for: {}", source.getName(), e);
        }
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
        logger.warn("Generating mock data for symbol: {} (external APIs unavailable)", request.getSymbol());
        
        MarketDataResponse response = new MarketDataResponse();
        response.setSymbol(request.getSymbol());
        response.setDataType(request.getDataType());
        response.setSource("mock");
        response.setDataTimestamp(LocalDateTime.now());
        response.setQualityScore(50); // Lower quality for mock data
        response.setMessage("Mock data - external APIs unavailable");
        
        if (request.getDataType() == DataType.LIVE_PRICE) {
            BigDecimal basePrice = getBasePrice(request.getSymbol());
            BigDecimal volatility = getSymbolVolatility(request.getSymbol());
            BigDecimal priceChange = BigDecimal.valueOf(random.nextGaussian()).multiply(volatility).multiply(basePrice);
            BigDecimal currentPrice = basePrice.add(priceChange);
            
            response.setPrice(currentPrice);
            response.setBidPrice(currentPrice.multiply(BigDecimal.valueOf(0.999)));
            response.setAskPrice(currentPrice.multiply(BigDecimal.valueOf(1.001)));
            response.setDayChange(priceChange);
            response.setDayChangePercent(priceChange.divide(basePrice, 6, RoundingMode.HALF_UP));
            response.setVolume(BigDecimal.valueOf(100000 + random.nextInt(500000)));
            
        } else if (request.getDataType() == DataType.OHLCV) {
            response.setTimeframe(request.getTimeframe());
            response.setOhlcvData(generateMockOHLCVData(request));
        }
        
        return response;
    }
    
    private List<MarketDataResponse.OHLCVData> generateMockOHLCVData(MarketDataRequest request) {
        List<MarketDataResponse.OHLCVData> data = new ArrayList<>();
        BigDecimal basePrice = getBasePrice(request.getSymbol());
        BigDecimal volatility = getSymbolVolatility(request.getSymbol());
        BigDecimal currentPrice = basePrice;
        
        LocalDateTime start = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now().minusDays(30);
        LocalDateTime end = request.getEndTime() != null ? request.getEndTime() : LocalDateTime.now();
        
        LocalDateTime current = start;
        while (current.isBefore(end) && data.size() < (request.getLimit() != null ? request.getLimit() : 100)) {
            BigDecimal open = currentPrice;
            BigDecimal change = BigDecimal.valueOf(random.nextGaussian()).multiply(volatility).multiply(open);
            BigDecimal close = open.add(change);
            
            BigDecimal high = open.max(close).add(open.multiply(BigDecimal.valueOf(random.nextDouble() * 0.01)));
            BigDecimal low = open.min(close).subtract(open.multiply(BigDecimal.valueOf(random.nextDouble() * 0.01)));
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
        if (timeframe == null) return current.plusDays(1);
        
        switch (timeframe.toLowerCase()) {
            case "1m": return current.plusMinutes(1);
            case "5m": return current.plusMinutes(5);
            case "15m": return current.plusMinutes(15);
            case "30m": return current.plusMinutes(30);
            case "1h": return current.plusHours(1);
            case "4h": return current.plusHours(4);
            case "1d": return current.plusDays(1);
            case "1w": return current.plusWeeks(1);
            default: return current.plusDays(1);
        }
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
    
    // Legacy method for backward compatibility - now uses the new centralized logic
    public Map<String, Object> getMarketData(String symbol, String timeframe) {
        try {
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
        } catch (Exception e) {
            logger.error("Legacy getMarketData failed for symbol: {}", symbol, e);
            
            // Fallback to basic mock data
            Map<String, Object> data = new HashMap<>();
            BigDecimal basePrice = getBasePrice(symbol);
            data.put("symbol", symbol);
            data.put("price", basePrice);
            data.put("bid", basePrice.multiply(BigDecimal.valueOf(0.999)));
            data.put("ask", basePrice.multiply(BigDecimal.valueOf(1.001)));
            data.put("volume", BigDecimal.valueOf(100000));
            data.put("dayChange", BigDecimal.ZERO);
            data.put("dayChangePercent", BigDecimal.ZERO);
            data.put("timestamp", LocalDateTime.now());
            data.put("source", "mock");
            
            return data;
        }
    }
}