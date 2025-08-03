package com.quantcrux.repository;

import com.quantcrux.model.Strategy;
import com.quantcrux.model.StrategySignal;
import com.quantcrux.model.SignalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StrategySignalRepository extends JpaRepository<StrategySignal, UUID> {
    
    List<StrategySignal> findByStrategyOrderByCreatedAtDesc(Strategy strategy);
    
    List<StrategySignal> findByStrategyAndSignalType(Strategy strategy, SignalType signalType);
    
    @Query("SELECT ss FROM StrategySignal ss WHERE ss.strategy = :strategy AND ss.createdAt >= :fromDate ORDER BY ss.createdAt DESC")
    List<StrategySignal> findByStrategyAndCreatedAtAfter(@Param("strategy") Strategy strategy, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(ss) FROM StrategySignal ss WHERE ss.strategy = :strategy AND ss.signalType = :signalType")
    long countByStrategyAndSignalType(@Param("strategy") Strategy strategy, @Param("signalType") SignalType signalType);
}