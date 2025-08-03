package com.quantcrux.repository;

import com.quantcrux.model.Portfolio;
import com.quantcrux.model.PortfolioStatus;
import com.quantcrux.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    
    List<Portfolio> findByOwner(User owner);
    
    List<Portfolio> findByOwnerOrderByCreatedAtDesc(User owner);
    
    List<Portfolio> findByManager(User manager);
    
    List<Portfolio> findByManagerOrderByCreatedAtDesc(User manager);
    
    @Query("SELECT p FROM Portfolio p WHERE p.owner = :user OR p.manager = :user ORDER BY p.createdAt DESC")
    List<Portfolio> findByOwnerOrManagerOrderByCreatedAtDesc(@Param("user") User user);
    
    List<Portfolio> findByOwnerAndStatus(User owner, PortfolioStatus status);
    
    List<Portfolio> findByManagerAndStatus(User manager, PortfolioStatus status);
    
    @Query("SELECT p FROM Portfolio p WHERE (p.owner = :user OR p.manager = :user) AND p.name ILIKE %:name%")
    List<Portfolio> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);
    
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.owner = :user AND p.status = :status")
    long countByOwnerAndStatus(@Param("user") User user, @Param("status") PortfolioStatus status);
    
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.manager = :user AND p.status = :status")
    long countByManagerAndStatus(@Param("user") User user, @Param("status") PortfolioStatus status);
    
    @Query("SELECT p FROM Portfolio p WHERE p.id = :id AND (p.owner = :user OR p.manager = :user)")
    Optional<Portfolio> findByIdAndUser(@Param("id") UUID id, @Param("user") User user);
    
    @Query("SELECT SUM(p.currentNav) FROM Portfolio p WHERE p.owner = :user AND p.status = 'ACTIVE'")
    BigDecimal getTotalNavByOwner(@Param("user") User user);
    
    @Query("SELECT SUM(p.currentNav) FROM Portfolio p WHERE p.manager = :user AND p.status = 'ACTIVE'")
    BigDecimal getTotalManagedNavByManager(@Param("user") User user);
}