package com.quantcrux.repository;

import com.quantcrux.model.InstrumentType;
import com.quantcrux.model.Portfolio;
import com.quantcrux.model.PortfolioHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, UUID> {
    
    List<PortfolioHolding> findByPortfolio(Portfolio portfolio);
    
    List<PortfolioHolding> findByPortfolioOrderByWeightPctDesc(Portfolio portfolio);
    
    List<PortfolioHolding> findByPortfolioAndInstrumentType(Portfolio portfolio, InstrumentType instrumentType);
    
    Optional<PortfolioHolding> findByPortfolioAndSymbol(Portfolio portfolio, String symbol);
    
    @Query("SELECT ph FROM PortfolioHolding ph WHERE ph.portfolio = :portfolio AND ph.instrumentId = :instrumentId")
    Optional<PortfolioHolding> findByPortfolioAndInstrumentId(@Param("portfolio") Portfolio portfolio, @Param("instrumentId") UUID instrumentId);
    
    @Query("SELECT SUM(ph.marketValue) FROM PortfolioHolding ph WHERE ph.portfolio = :portfolio")
    BigDecimal getTotalMarketValueByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT SUM(ph.unrealizedPnl) FROM PortfolioHolding ph WHERE ph.portfolio = :portfolio")
    BigDecimal getTotalUnrealizedPnlByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT ph.sector, SUM(ph.marketValue) FROM PortfolioHolding ph WHERE ph.portfolio = :portfolio AND ph.sector IS NOT NULL GROUP BY ph.sector")
    List<Object[]> getSectorAllocationByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT ph.assetClass, SUM(ph.marketValue) FROM PortfolioHolding ph WHERE ph.portfolio = :portfolio AND ph.assetClass IS NOT NULL GROUP BY ph.assetClass")
    List<Object[]> getAssetClassAllocationByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT COUNT(ph) FROM PortfolioHolding ph WHERE ph.portfolio = :portfolio AND ph.quantity > 0")
    long countActivePositionsByPortfolio(@Param("portfolio") Portfolio portfolio);
}