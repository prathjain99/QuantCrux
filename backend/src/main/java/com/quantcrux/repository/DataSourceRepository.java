package com.quantcrux.repository;

import com.quantcrux.model.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, UUID> {
    
    Optional<DataSource> findByName(String name);
    
    List<DataSource> findByIsActiveTrueOrderByPriorityDesc();
    
    @Query("SELECT ds FROM DataSource ds WHERE ds.isActive = true AND ds.supportsLivePrices = true ORDER BY ds.priority DESC")
    List<DataSource> findActiveLivePriceSources();
    
    @Query("SELECT ds FROM DataSource ds WHERE ds.isActive = true AND ds.supportsHistorical = true ORDER BY ds.priority DESC")
    List<DataSource> findActiveHistoricalSources();
    
    @Query("SELECT ds FROM DataSource ds WHERE ds.isActive = true AND ds.supportsCrypto = true ORDER BY ds.priority DESC")
    List<DataSource> findActiveCryptoSources();
    
    @Query("SELECT ds FROM DataSource ds WHERE ds.isActive = true AND ds.consecutiveFailures < 5 ORDER BY ds.priority DESC")
    List<DataSource> findHealthySources();
    
    @Query("SELECT ds FROM DataSource ds WHERE ds.requestsToday < ds.rateLimitPerDay AND ds.requestsThisMinute < ds.rateLimitPerMinute ORDER BY ds.priority DESC")
    List<DataSource> findSourcesWithinRateLimit();
    
    @Modifying
    @Query("UPDATE DataSource ds SET ds.requestsThisMinute = 0")
    int resetMinuteCounters();
    
    @Modifying
    @Query("UPDATE DataSource ds SET ds.requestsToday = 0, ds.requestsThisMinute = 0")
    int resetDailyCounters();
    
    @Query("SELECT ds FROM DataSource ds WHERE ds.lastErrorAt > :since ORDER BY ds.lastErrorAt DESC")
    List<DataSource> findRecentErrors(@Param("since") LocalDateTime since);
}