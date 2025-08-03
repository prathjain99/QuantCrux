package com.quantcrux.repository;

import com.quantcrux.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TradeRepository extends JpaRepository<Trade, UUID> {
    
    List<Trade> findByUser(User user);
    
    List<Trade> findByUserOrderByExecutedAtDesc(User user);
    
    List<Trade> findByPortfolio(Portfolio portfolio);
    
    List<Trade> findByPortfolioOrderByExecutedAtDesc(Portfolio portfolio);
    
    List<Trade> findByOrder(Order order);
    
    List<Trade> findBySymbol(String symbol);
    
    List<Trade> findByUserAndSymbol(User user, String symbol);
    
    @Query("SELECT t FROM Trade t WHERE (t.user = :user OR EXISTS (SELECT 1 FROM Portfolio p WHERE p.id = t.portfolio.id AND p.manager = :user)) ORDER BY t.executedAt DESC")
    List<Trade> findByUserOrManagedPortfoliosOrderByExecutedAtDesc(@Param("user") User user);
    
    @Query("SELECT t FROM Trade t WHERE t.portfolio = :portfolio AND t.tradeDate >= :fromDate ORDER BY t.executedAt DESC")
    List<Trade> findByPortfolioAndTradeDateAfter(@Param("portfolio") Portfolio portfolio, @Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT t FROM Trade t WHERE t.user = :user AND t.executedAt >= :fromDateTime ORDER BY t.executedAt DESC")
    List<Trade> findByUserAndExecutedAtAfter(@Param("user") User user, @Param("fromDateTime") LocalDateTime fromDateTime);
    
    @Query("SELECT SUM(CASE WHEN t.side = 'BUY' THEN t.totalAmount ELSE -t.totalAmount END) FROM Trade t WHERE t.portfolio = :portfolio AND t.status = 'EXECUTED'")
    BigDecimal getTotalTradeVolumeByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT COUNT(t) FROM Trade t WHERE t.user = :user AND t.tradeDate = :date")
    long countByUserAndTradeDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT t FROM Trade t WHERE t.portfolio = :portfolio AND t.symbol = :symbol ORDER BY t.executedAt DESC")
    List<Trade> findByPortfolioAndSymbolOrderByExecutedAtDesc(@Param("portfolio") Portfolio portfolio, @Param("symbol") String symbol);
}