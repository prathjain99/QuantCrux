package com.quantcrux.repository;

import com.quantcrux.model.Backtest;
import com.quantcrux.model.BacktestStatus;
import com.quantcrux.model.Strategy;
import com.quantcrux.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BacktestRepository extends JpaRepository<Backtest, UUID> {
    
    List<Backtest> findByUser(User user);
    
    List<Backtest> findByUserOrderByCreatedAtDesc(User user);
    
    List<Backtest> findByStrategy(Strategy strategy);
    
    List<Backtest> findByUserAndStatus(User user, BacktestStatus status);
    
    List<Backtest> findBySymbol(String symbol);
    
    @Query("SELECT b FROM Backtest b WHERE b.user = :user AND b.name ILIKE %:name%")
    List<Backtest> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);
    
    @Query("SELECT COUNT(b) FROM Backtest b WHERE b.user = :user AND b.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") BacktestStatus status);
    
    @Query("SELECT b FROM Backtest b WHERE b.status = :status AND b.createdAt < :cutoffTime")
    List<Backtest> findStaleBacktests(@Param("status") BacktestStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);
    
    Optional<Backtest> findByIdAndUser(UUID id, User user);
    
    @Query("SELECT b FROM Backtest b WHERE b.user = :user AND b.strategy = :strategy ORDER BY b.createdAt DESC")
    List<Backtest> findByUserAndStrategyOrderByCreatedAtDesc(@Param("user") User user, @Param("strategy") Strategy strategy);
}