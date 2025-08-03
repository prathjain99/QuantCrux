package com.quantcrux.service;

import com.quantcrux.model.DataSource;
import com.quantcrux.model.DataType;
import com.quantcrux.repository.DataSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import com.quantcrux.dto.MarketDataRequest;





import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DataSourceHealthService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSourceHealthService.class);
    
    @Autowired
    private DataSourceRepository dataSourceRepository;
    
    @Autowired
    private MarketDataService marketDataService;
    
    /**
     * Reset minute counters every minute
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void resetMinuteCounters() {
        try {
            int updated = dataSourceRepository.resetMinuteCounters();
            logger.debug("Reset minute counters for {} data sources", updated);
        } catch (Exception e) {
            logger.error("Failed to reset minute counters", e);
        }
    }
    
    /**
     * Reset daily counters at midnight
     */
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void resetDailyCounters() {
        try {
            int updated = dataSourceRepository.resetDailyCounters();
            logger.info("Reset daily counters for {} data sources", updated);
        } catch (Exception e) {
            logger.error("Failed to reset daily counters", e);
        }
    }
    
    /**
     * Health check for data sources every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void performHealthCheck() {
        try {
            List<DataSource> activeSources = dataSourceRepository.findByIsActiveTrueOrderByPriorityDesc();
            
            for (DataSource source : activeSources) {
                // Skip sources with too many consecutive failures
                if (source.getConsecutiveFailures() >= 5) {
                    logger.warn("Skipping health check for source {} due to {} consecutive failures", 
                              source.getName(), source.getConsecutiveFailures());
                    continue;
                }
                
                // Perform a simple health check
                performSourceHealthCheck(source);
            }
            
        } catch (Exception e) {
            logger.error("Failed to perform health check", e);
        }
    }
    
    /**
     * Clean expired cache entries every hour
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanExpiredCache() {
        try {
            marketDataService.refreshCache();
            logger.info("Cleaned expired cache entries");
        } catch (Exception e) {
            logger.error("Failed to clean expired cache", e);
        }
    }
    
    private void performSourceHealthCheck(DataSource source) {
        try {
            // Test with a simple symbol request
            MarketDataRequest testRequest = new MarketDataRequest("AAPL", DataType.LIVE_PRICE);
            testRequest.setPreferredSource(source.getName());
            
            // This will test the source and update its status
            marketDataService.getMarketData(testRequest);
            
            logger.debug("Health check passed for source: {}", source.getName());
            
        } catch (Exception e) {
            logger.warn("Health check failed for source {}: {}", source.getName(), e.getMessage());
            
            // Update failure count
            source.setConsecutiveFailures(source.getConsecutiveFailures() + 1);
            source.setLastErrorMessage("Health check failed: " + e.getMessage());
            source.setLastErrorAt(LocalDateTime.now());
            
            // Disable source if too many failures
            if (source.getConsecutiveFailures() >= 10) {
                source.setIsActive(false);
                logger.error("Disabled data source {} due to {} consecutive failures", 
                           source.getName(), source.getConsecutiveFailures());
            }
            
            dataSourceRepository.save(source);
        }
    }
    
    public void enableDataSource(String sourceName) {
        dataSourceRepository.findByName(sourceName).ifPresent(source -> {
            source.setIsActive(true);
            source.setConsecutiveFailures(0);
            source.setLastErrorMessage(null);
            dataSourceRepository.save(source);
            logger.info("Enabled data source: {}", sourceName);
        });
    }
    
    public void disableDataSource(String sourceName) {
        dataSourceRepository.findByName(sourceName).ifPresent(source -> {
            source.setIsActive(false);
            dataSourceRepository.save(source);
            logger.info("Disabled data source: {}", sourceName);
        });
    }
    
    public List<DataSource> getDataSourceStatus() {
        return dataSourceRepository.findAll();
    }
}