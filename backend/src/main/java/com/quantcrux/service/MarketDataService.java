package com.quantcrux.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class MarketDataService {
    
    private final Random random = new Random();
    
    public Map<String, Object> getMarketData(String symbol, String timeframe) {
        // Simulate market data - in a real implementation, this would fetch from external APIs
        Map<String, Object> data = new HashMap<>();
        
        // Generate realistic price data based on symbol
        BigDecimal basePrice = getBasePrice(symbol);
        BigDecimal currentPrice = basePrice.add(BigDecimal.valueOf((random.nextGaussian() * 5)))
                .setScale(2, RoundingMode.HALF_UP);
        
        data.put("symbol", symbol);
        data.put("price", currentPrice);
        data.put("open", currentPrice.subtract(BigDecimal.valueOf(random.nextDouble() * 2)));
        data.put("high", currentPrice.add(BigDecimal.valueOf(random.nextDouble() * 3)));
        data.put("low", currentPrice.subtract(BigDecimal.valueOf(random.nextDouble() * 3)));
        data.put("volume", random.nextInt(1000000) + 100000);
        data.put("timeframe", timeframe);
        data.put("timestamp", System.currentTimeMillis());
        
        return data;
    }
    
    public Map<String, Object> getIndicators(String symbol, String timeframe) {
        Map<String, Object> indicators = new HashMap<>();
        
        // Simulate technical indicators
        indicators.put("RSI", 30 + random.nextDouble() * 40); // RSI between 30-70
        indicators.put("SMA_20", 145 + random.nextGaussian() * 10);
        indicators.put("SMA_50", 140 + random.nextGaussian() * 15);
        indicators.put("EMA_20", 147 + random.nextGaussian() * 8);
        indicators.put("MACD", random.nextGaussian() * 2);
        indicators.put("BB_UPPER", 155 + random.nextGaussian() * 5);
        indicators.put("BB_LOWER", 135 + random.nextGaussian() * 5);
        
        return indicators;
    }
    
    private BigDecimal getBasePrice(String symbol) {
        // Return realistic base prices for common symbols
        switch (symbol.toUpperCase()) {
            case "AAPL": return BigDecimal.valueOf(150.00);
            case "GOOGL": return BigDecimal.valueOf(2500.00);
            case "MSFT": return BigDecimal.valueOf(300.00);
            case "TSLA": return BigDecimal.valueOf(200.00);
            case "BTCUSD": return BigDecimal.valueOf(45000.00);
            case "ETHUSD": return BigDecimal.valueOf(3000.00);
            case "NIFTY": return BigDecimal.valueOf(18000.00);
            case "SENSEX": return BigDecimal.valueOf(60000.00);
            default: return BigDecimal.valueOf(100.00);
        }
    }
}