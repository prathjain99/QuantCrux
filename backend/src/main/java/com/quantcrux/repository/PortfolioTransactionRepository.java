package com.quantcrux.repository;

import com.quantcrux.model.Portfolio;
import com.quantcrux.model.PortfolioTransaction;
import com.quantcrux.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioTransactionRepository extends JpaRepository<PortfolioTransaction, UUID> {
    
    List<PortfolioTransaction> findByPortfolioOrderByExecutedAtDesc(Portfolio portfolio);
    
    List<PortfolioTransaction> findByPortfolioAndTransactionType(Portfolio portfolio, TransactionType transactionType);
    
    @Query("SELECT pt FROM PortfolioTransaction pt WHERE pt.portfolio = :portfolio AND pt.executedAt >= :fromDate ORDER BY pt.executedAt DESC")
    List<PortfolioTransaction> findByPortfolioAndExecutedAtAfter(@Param("portfolio") Portfolio portfolio, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT pt FROM PortfolioTransaction pt WHERE pt.portfolio = :portfolio ORDER BY pt.executedAt DESC")
    List<PortfolioTransaction> findRecentByPortfolio(@Param("portfolio") Portfolio portfolio);
    
    @Query("SELECT SUM(pt.amount) FROM PortfolioTransaction pt WHERE pt.portfolio = :portfolio AND pt.transactionType = :transactionType")
    Double getTotalAmountByPortfolioAndType(@Param("portfolio") Portfolio portfolio, @Param("transactionType") TransactionType transactionType);
    
    @Query("SELECT COUNT(pt) FROM PortfolioTransaction pt WHERE pt.portfolio = :portfolio AND pt.transactionType IN ('BUY', 'SELL')")
    long countTradesByPortfolio(@Param("portfolio") Portfolio portfolio);
}