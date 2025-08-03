package com.quantcrux.repository;

import com.quantcrux.model.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, UUID> {
    
    List<MarketData> findBySymbolAndTimeframe(String symbol, String timeframe);
    
    @Query("SELECT md FROM MarketData md WHERE md.symbol = :symbol AND md.timeframe = :timeframe " +
           "AND md.timestamp >= :startTime AND md.timestamp <= :endTime ORDER BY md.timestamp")
    List<MarketData> findBySymbolAndTimeframeAndTimestampBetween(
        @Param("symbol") String symbol, 
        @Param("timeframe") String timeframe,
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime
    );
    
    Optional<MarketData> findBySymbolAndTimeframeAndTimestamp(String symbol, String timeframe, LocalDateTime timestamp);
    
    @Query("SELECT md FROM MarketData md WHERE md.symbol = :symbol AND md.timeframe = :timeframe " +
           "ORDER BY md.timestamp DESC")
    List<MarketData> findBySymbolAndTimeframeOrderByTimestampDesc(@Param("symbol") String symbol, @Param("timeframe") String timeframe);
    
    @Query("SELECT md FROM MarketData md WHERE md.symbol = :symbol AND md.timeframe = :timeframe " +
           "AND md.timestamp <= :beforeTime ORDER BY md.timestamp DESC")
    List<MarketData> findLatestBefore(@Param("symbol") String symbol, @Param("timeframe") String timeframe, @Param("beforeTime") LocalDateTime beforeTime);
    
    @Query("SELECT DISTINCT md.symbol FROM MarketData md")
    List<String> findDistinctSymbols();
    
    @Query("SELECT COUNT(md) FROM MarketData md WHERE md.symbol = :symbol AND md.timeframe = :timeframe")
    long countBySymbolAndTimeframe(@Param("symbol") String symbol, @Param("timeframe") String timeframe);
}