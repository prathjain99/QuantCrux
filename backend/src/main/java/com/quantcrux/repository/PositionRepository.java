package com.quantcrux.repository;

import com.quantcrux.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
    
    List<Position> findByPortfolio(Portfolio portfolio);
    
    List<Position> findByPortfolioOrderByMarketValueDesc(Portfolio portfolio);
    
    Optional<Position> findByPortfolioAndSymbolAndInstrumentType(Portfolio portfolio, String symbol, InstrumentType instrumentType);
    
    List<Position> findByPortfolioAndInstrumentType(Portfolio portfolio, InstrumentType instrumentType);
    
    @Query("SELECT p FROM Position p WHERE p.portfolio = :portfolio AND p.netQuantity != 0 ORDER BY p.marketValue DESC")
    List<Position> findActivePositionsByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT p FROM Position p WHERE p.portfolio IN :portfolios ORDER BY p.marketValue DESC")
    List<Position> findByPortfoliosOrderByMarketValueDesc(@Param("portfolios") List<Portfolio> portfolios);
    
    @Query("SELECT SUM(p.marketValue) FROM Position p WHERE p.portfolio = :portfolio AND p.netQuantity > 0")
    BigDecimal getTotalLongMarketValueByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT SUM(p.marketValue) FROM Position p WHERE p.portfolio = :portfolio AND p.netQuantity < 0")
    BigDecimal getTotalShortMarketValueByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT SUM(p.unrealizedPnl) FROM Position p WHERE p.portfolio = :portfolio")
    BigDecimal getTotalUnrealizedPnlByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT COUNT(p) FROM Position p WHERE p.portfolio = :portfolio AND p.netQuantity != 0")
    long countActivePositionsByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT p FROM Position p WHERE p.portfolio.owner = :user OR p.portfolio.manager = :user ORDER BY p.updatedAt DESC")
    List<Position> findByUserPortfoliosOrderByUpdatedAtDesc(@Param("user") User user);
}