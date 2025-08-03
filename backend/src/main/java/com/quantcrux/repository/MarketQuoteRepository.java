package com.quantcrux.repository;

import com.quantcrux.model.InstrumentType;
import com.quantcrux.model.MarketQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketQuoteRepository extends JpaRepository<MarketQuote, UUID> {
    
    Optional<MarketQuote> findBySymbolAndInstrumentType(String symbol, InstrumentType instrumentType);
    
    Optional<MarketQuote> findBySymbolAndInstrumentTypeAndMarketDate(String symbol, InstrumentType instrumentType, LocalDate marketDate);
    
    List<MarketQuote> findByInstrumentType(InstrumentType instrumentType);
    
    List<MarketQuote> findByMarketDate(LocalDate marketDate);
    
    @Query("SELECT mq FROM MarketQuote mq WHERE mq.symbol IN :symbols AND mq.instrumentType = :instrumentType ORDER BY mq.quoteTime DESC")
    List<MarketQuote> findBySymbolsAndInstrumentType(@Param("symbols") List<String> symbols, @Param("instrumentType") InstrumentType instrumentType);
    
    @Query("SELECT mq FROM MarketQuote mq WHERE mq.quoteTime >= :fromTime ORDER BY mq.quoteTime DESC")
    List<MarketQuote> findByQuoteTimeAfter(@Param("fromTime") LocalDateTime fromTime);
    
    @Query("SELECT DISTINCT mq.symbol FROM MarketQuote mq WHERE mq.instrumentType = :instrumentType")
    List<String> findDistinctSymbolsByInstrumentType(@Param("instrumentType") InstrumentType instrumentType);
    
    @Query("SELECT mq FROM MarketQuote mq WHERE mq.symbol = :symbol ORDER BY mq.quoteTime DESC")
    List<MarketQuote> findBySymbolOrderByQuoteTimeDesc(@Param("symbol") String symbol);
    
    @Query("SELECT mq FROM MarketQuote mq WHERE mq.marketDate = CURRENT_DATE ORDER BY mq.updatedAt DESC")
    List<MarketQuote> findTodaysQuotesOrderByUpdatedAtDesc();
}