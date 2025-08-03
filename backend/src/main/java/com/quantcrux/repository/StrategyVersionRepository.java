package com.quantcrux.repository;

import com.quantcrux.model.Strategy;
import com.quantcrux.model.StrategyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StrategyVersionRepository extends JpaRepository<StrategyVersion, UUID> {
    
    List<StrategyVersion> findByStrategyOrderByVersionNumberDesc(Strategy strategy);
    
    Optional<StrategyVersion> findByStrategyAndVersionNumber(Strategy strategy, Integer versionNumber);
    
    @Query("SELECT MAX(sv.versionNumber) FROM StrategyVersion sv WHERE sv.strategy = :strategy")
    Optional<Integer> findMaxVersionNumberByStrategy(@Param("strategy") Strategy strategy);
    
    @Query("SELECT sv FROM StrategyVersion sv WHERE sv.strategy = :strategy ORDER BY sv.createdAt DESC")
    List<StrategyVersion> findByStrategyOrderByCreatedAtDesc(@Param("strategy") Strategy strategy);
}