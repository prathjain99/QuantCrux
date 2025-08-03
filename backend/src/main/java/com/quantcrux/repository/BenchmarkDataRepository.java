package com.quantcrux.repository;

import com.quantcrux.model.BenchmarkData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BenchmarkDataRepository extends JpaRepository<BenchmarkData, UUID> {
    
    List<BenchmarkData> findBySymbolOrderByDateDesc(String symbol);
    
    Optional<BenchmarkData> findBySymbolAndDate(String symbol, LocalDate date);
    
    @Query("SELECT bd FROM BenchmarkData bd WHERE bd.symbol = :symbol AND bd.date >= :fromDate AND bd.date <= :toDate ORDER BY bd.date")
    List<BenchmarkData> findBySymbolAndDateBetween(@Param("symbol") String symbol, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
    
    @Query("SELECT bd FROM BenchmarkData bd WHERE bd.symbol = :symbol AND bd.date >= :fromDate ORDER BY bd.date DESC")
    List<BenchmarkData> findBySymbolAndDateAfter(@Param("symbol") String symbol, @Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT bd FROM BenchmarkData bd WHERE bd.symbol = :symbol ORDER BY bd.date DESC")
    Optional<BenchmarkData> findLatestBySymbol(@Param("symbol") String symbol);
    
    @Query("SELECT DISTINCT bd.symbol FROM BenchmarkData bd ORDER BY bd.symbol")
    List<String> findDistinctSymbols();
    
    @Query("SELECT COUNT(bd) FROM BenchmarkData bd WHERE bd.symbol = :symbol")
    long countBySymbol(@Param("symbol") String symbol);
    
    @Query("SELECT bd FROM BenchmarkData bd WHERE bd.date = :date ORDER BY bd.symbol")
    List<BenchmarkData> findByDate(@Param("date") LocalDate date);
}