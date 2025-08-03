package com.quantcrux.repository;

import com.quantcrux.model.Backtest;
import com.quantcrux.model.BacktestTrade;
import com.quantcrux.model.SignalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BacktestTradeRepository extends JpaRepository<BacktestTrade, UUID> {
    
    List<BacktestTrade> findByBacktest(Backtest backtest);
    
    List<BacktestTrade> findByBacktestOrderByTradeNumber(Backtest backtest);
    
    List<BacktestTrade> findByBacktestAndSignalType(Backtest backtest, SignalType signalType);
    
    @Query("SELECT bt FROM BacktestTrade bt WHERE bt.backtest = :backtest AND bt.entryTime >= :fromTime ORDER BY bt.entryTime")
    List<BacktestTrade> findByBacktestAndEntryTimeAfter(@Param("backtest") Backtest backtest, @Param("fromTime") LocalDateTime fromTime);
    
    @Query("SELECT COUNT(bt) FROM BacktestTrade bt WHERE bt.backtest = :backtest AND bt.netPnl > 0")
    long countWinningTrades(@Param("backtest") Backtest backtest);
    
    @Query("SELECT COUNT(bt) FROM BacktestTrade bt WHERE bt.backtest = :backtest AND bt.netPnl < 0")
    long countLosingTrades(@Param("backtest") Backtest backtest);
    
    @Query("SELECT AVG(bt.netPnl) FROM BacktestTrade bt WHERE bt.backtest = :backtest")
    Double getAverageNetPnl(@Param("backtest") Backtest backtest);
    
    @Query("SELECT SUM(bt.netPnl) FROM BacktestTrade bt WHERE bt.backtest = :backtest AND bt.netPnl > 0")
    Double getTotalWinningPnl(@Param("backtest") Backtest backtest);
    
    @Query("SELECT SUM(ABS(bt.netPnl)) FROM BacktestTrade bt WHERE bt.backtest = :backtest AND bt.netPnl < 0")
    Double getTotalLosingPnl(@Param("backtest") Backtest backtest);
}