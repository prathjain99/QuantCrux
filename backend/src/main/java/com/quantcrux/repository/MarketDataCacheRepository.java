package com.quantcrux.repository;

import com.quantcrux.model.DataType;
import com.quantcrux.model.MarketDataCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketDataCacheRepository extends JpaRepository<MarketDataCache, UUID> {
    
    Optional<MarketDataCache> findBySymbolAndDataTypeAndTimeframe(String symbol, DataType dataType, String timeframe);
    
    Optional<MarketDataCache> findBySymbolAndDataType(String symbol, DataType dataType);
    
    List<MarketDataCache> findBySymbolAndDataTypeAndExpiresAtAfter(String symbol, DataType dataType, LocalDateTime cutoff);
    
    List<MarketDataCache> findByDataTypeAndExpiresAtAfter(DataType dataType, LocalDateTime cutoff);
    
    @Query("SELECT mdc FROM MarketDataCache mdc WHERE mdc.symbol = :symbol AND mdc.dataType = :dataType AND mdc.expiresAt > CURRENT_TIMESTAMP ORDER BY mdc.dataTimestamp DESC")
    Optional<MarketDataCache> findLatestValidBySymbolAndDataType(@Param("symbol") String symbol, @Param("dataType") DataType dataType);
    
    @Query("SELECT mdc FROM MarketDataCache mdc WHERE mdc.symbol IN :symbols AND mdc.dataType = :dataType AND mdc.expiresAt > CURRENT_TIMESTAMP")
    List<MarketDataCache> findValidBySymbolsAndDataType(@Param("symbols") List<String> symbols, @Param("dataType") DataType dataType);
    
    @Modifying
    @Query("DELETE FROM MarketDataCache mdc WHERE mdc.expiresAt < CURRENT_TIMESTAMP")
    int deleteExpiredEntries();
    
    @Query("SELECT COUNT(mdc) FROM MarketDataCache mdc WHERE mdc.source = :source AND mdc.createdAt >= :since")
    long countBySourceAndCreatedAtAfter(@Param("source") String source, @Param("since") LocalDateTime since);
    
    @Query("SELECT DISTINCT mdc.symbol FROM MarketDataCache mdc WHERE mdc.dataType = :dataType AND mdc.expiresAt > CURRENT_TIMESTAMP")
    List<String> findDistinctValidSymbolsByDataType(@Param("dataType") DataType dataType);
    
    List<MarketDataCache> findBySourceAndCreatedAtAfter(String source, LocalDateTime since);
}