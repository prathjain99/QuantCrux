package com.quantcrux.repository;

import com.quantcrux.model.Portfolio;
import com.quantcrux.model.PortfolioHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioHistoryRepository extends JpaRepository<PortfolioHistory, UUID> {
    
    List<PortfolioHistory> findByPortfolioOrderByDateDesc(Portfolio portfolio);
    
    @Query("SELECT ph FROM PortfolioHistory ph WHERE ph.portfolio = :portfolio AND ph.date >= :fromDate ORDER BY ph.date")
    List<PortfolioHistory> findByPortfolioAndDateAfter(@Param("portfolio") Portfolio portfolio, @Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT ph FROM PortfolioHistory ph WHERE ph.portfolio = :portfolio AND ph.date >= :fromDate AND ph.date <= :toDate ORDER BY ph.date")
    List<PortfolioHistory> findByPortfolioAndDateBetween(@Param("portfolio") Portfolio portfolio, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
    
    Optional<PortfolioHistory> findByPortfolioAndDate(Portfolio portfolio, LocalDate date);
    
    @Query("SELECT ph FROM PortfolioHistory ph WHERE ph.portfolio = :portfolio ORDER BY ph.date DESC")
    Optional<PortfolioHistory> findLatestByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT COUNT(ph) FROM PortfolioHistory ph WHERE ph.portfolio = :portfolio")
    long countByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT AVG(ph.dailyReturnPct) FROM PortfolioHistory ph WHERE ph.portfolio = :portfolio AND ph.date >= :fromDate")
    Double getAverageDailyReturnSince(@Param("portfolio") Portfolio portfolio, @Param("fromDate") LocalDate fromDate);
}