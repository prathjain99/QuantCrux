package com.quantcrux.repository;

import com.quantcrux.model.Product;
import com.quantcrux.model.ProductPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductPricingRepository extends JpaRepository<ProductPricing, UUID> {
    
    List<ProductPricing> findByProductOrderByPricingDateDesc(Product product);
    
    @Query("SELECT pp FROM ProductPricing pp WHERE pp.product = :product AND pp.pricingDate >= :fromDate ORDER BY pp.pricingDate DESC")
    List<ProductPricing> findByProductAndPricingDateAfter(@Param("product") Product product, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT pp FROM ProductPricing pp WHERE pp.product = :product ORDER BY pp.pricingDate DESC")
    Optional<ProductPricing> findLatestPricingByProduct(@Param("product") Product product);
    
    @Query("SELECT COUNT(pp) FROM ProductPricing pp WHERE pp.product = :product")
    long countByProduct(@Param("product") Product product);
}