package com.quantcrux.repository;

import com.quantcrux.model.Strategy;
import com.quantcrux.model.StrategyStatus;
import com.quantcrux.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, UUID> {
    
    List<Strategy> findByUser(User user);
    
    List<Strategy> findByUserAndStatus(User user, StrategyStatus status);
    
    List<Strategy> findBySymbol(String symbol);
    
    @Query("SELECT s FROM Strategy s WHERE s.user = :user AND s.name ILIKE %:name%")
    List<Strategy> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);
    
    @Query("SELECT s FROM Strategy s JOIN s.tags t WHERE s.user = :user AND t = :tag")
    List<Strategy> findByUserAndTag(@Param("user") User user, @Param("tag") String tag);
    
    @Query("SELECT COUNT(s) FROM Strategy s WHERE s.user = :user AND s.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") StrategyStatus status);
    
    Optional<Strategy> findByIdAndUser(UUID id, User user);
}