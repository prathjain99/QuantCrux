package com.quantcrux.repository;

import com.quantcrux.model.AnalyticsRisk;
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
public interface AnalyticsRiskRepository extends JpaRepository<AnalyticsRisk, UUID> {
    
    List<AnalyticsRisk> findByPortfolioOrderByCalculationDateDesc(Portfolio portfolio);
    
    List<AnalyticsRisk> findByStrategyOrderByCalculationDateDesc(Strategy strategy);
    
    Optional<AnalyticsRisk> findByPortfolioAndCalculationDate(Portfolio portfolio, LocalDate calculationDate);
    
    Optional<AnalyticsRisk> findByStrategyAndCalculationDate(Strategy strategy, LocalDate calculationDate);
    
    @Query("SELECT ar FROM AnalyticsRisk ar WHERE ar.portfolio = :portfolio AND ar.calculationDate >= :fromDate ORDER BY ar.calculationDate DESC")
    List<AnalyticsRisk> findByPortfolioAndCalculationDateAfter(@Param("portfolio") Portfolio portfolio, @Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT ar FROM AnalyticsRisk ar WHERE ar.strategy = :strategy AND ar.calculationDate >= :fromDate ORDER BY ar.calculationDate DESC")
    List<AnalyticsRisk> findByStrategyAndCalculationDateAfter(@Param("strategy") Strategy strategy, @Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT ar FROM AnalyticsRisk ar WHERE ar.portfolio = :portfolio ORDER BY ar.calculationDate DESC")
    Optional<AnalyticsRisk> findLatestByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT ar FROM AnalyticsRisk ar WHERE ar.strategy = :strategy ORDER BY ar.calculationDate DESC")
    Optional<AnalyticsRisk> findLatestByStrategy(@Param("strategy") Strategy strategy);
}