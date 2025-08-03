package com.quantcrux.repository;

import com.quantcrux.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    List<Order> findByUser(User user);
    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    List<Order> findByPortfolio(Portfolio portfolio);
    
    List<Order> findByPortfolioOrderByCreatedAtDesc(Portfolio portfolio);
    
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    
    List<Order> findBySymbol(String symbol);
    
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.symbol ILIKE %:symbol%")
    List<Order> findByUserAndSymbolContaining(@Param("user") User user, @Param("symbol") String symbol);
    
    @Query("SELECT o FROM Order o WHERE (o.user = :user OR EXISTS (SELECT 1 FROM Portfolio p WHERE p.id = o.portfolio.id AND p.manager = :user)) ORDER BY o.createdAt DESC")
    List<Order> findByUserOrManagedPortfoliosOrderByCreatedAtDesc(@Param("user") User user);
    
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses AND o.expiresAt < :cutoffTime")
    List<Order> findExpiredOrders(@Param("statuses") List<OrderStatus> statuses, @Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user AND o.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") OrderStatus status);
    
    Optional<Order> findByClientOrderId(String clientOrderId);
    
    @Query("SELECT o FROM Order o WHERE o.portfolio = :portfolio AND o.createdAt >= :fromDate ORDER BY o.createdAt DESC")
    List<Order> findByPortfolioAndCreatedAtAfter(@Param("portfolio") Portfolio portfolio, @Param("fromDate") LocalDateTime fromDate);
}