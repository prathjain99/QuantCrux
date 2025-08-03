package com.quantcrux.repository;

import com.quantcrux.model.Product;
import com.quantcrux.model.ProductStatus;
import com.quantcrux.model.ProductType;
import com.quantcrux.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    List<Product> findByUser(User user);
    
    List<Product> findByUserOrderByCreatedAtDesc(User user);
    
    List<Product> findByUserAndStatus(User user, ProductStatus status);
    
    List<Product> findByUserAndProductType(User user, ProductType productType);
    
    List<Product> findByUnderlyingAsset(String underlyingAsset);
    
    @Query("SELECT p FROM Product p WHERE p.user = :user AND p.name ILIKE %:name%")
    List<Product> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);
    
    @Query("SELECT p FROM Product p WHERE p.user = :user AND p.underlyingAsset ILIKE %:asset%")
    List<Product> findByUserAndUnderlyingAssetContaining(@Param("user") User user, @Param("asset") String asset);
    
    @Query("SELECT p FROM Product p WHERE p.maturityDate <= :date AND p.status = 'ACTIVE'")
    List<Product> findExpiringProducts(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.user = :user AND p.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") ProductStatus status);
    
    Optional<Product> findByIdAndUser(UUID id, User user);
    
    @Query("SELECT p FROM Product p WHERE p.linkedStrategy.id = :strategyId")
    List<Product> findByLinkedStrategyId(@Param("strategyId") UUID strategyId);
}