package com.quantcrux.repository;

import com.quantcrux.model.AnalyticsPerformance;
import com.quantcrux.model.Portfolio;
import com.quantcrux.model.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalyticsPerformanceRepository extends JpaRepository<AnalyticsPerformance, UUID> {
    
    List<AnalyticsPerformance> findByPortfolioOrderByCalculationDateDesc(Portfolio portfolio);
    
    List<AnalyticsPerformance> findByStrategyOrderByCalculationDateDesc(Strategy strategy);
    
    Optional<AnalyticsPerformance> findByPortfolioAndCalculationDate(Portfolio portfolio, LocalDate calculationDate);
    
    Optional<AnalyticsPerformance> findByStrategyAndCalculationDate(Strategy strategy, LocalDate calculationDate);
    
    @Query("SELECT ap FROM AnalyticsPerformance ap WHERE ap.portfolio = :portfolio AND ap.calculationDate >= :fromDate ORDER BY ap.calculationDate DESC")
    List<AnalyticsPerformance> findByPortfolioAndCalculationDateAfter(@Param("portfolio") Portfolio portfolio, @Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT ap FROM AnalyticsPerformance ap WHERE ap.strategy = :strategy AND ap.calculationDate >= :fromDate ORDER BY ap.calculationDate DESC")
    List<AnalyticsPerformance> findByStrategyAndCalculationDateAfter(@Param("strategy") Strategy strategy, @Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT ap FROM AnalyticsPerformance ap WHERE ap.portfolio = :portfolio AND ap.periodStart >= :fromDate AND ap.periodEnd <= :toDate")
    List<AnalyticsPerformance> findByPortfolioAndPeriodBetween(@Param("portfolio") Portfolio portfolio, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}