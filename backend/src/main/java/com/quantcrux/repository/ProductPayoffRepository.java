package com.quantcrux.repository;

import com.quantcrux.model.Product;
import com.quantcrux.model.ProductPayoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductPayoffRepository extends JpaRepository<ProductPayoff, UUID> {
    
    List<ProductPayoff> findByProductOrderBySpotPrice(Product product);
    
    List<ProductPayoff> findByProductAndScenarioType(Product product, String scenarioType);
    
    @Query("SELECT pp FROM ProductPayoff pp WHERE pp.product = :product AND pp.scenarioType = :scenarioType ORDER BY pp.spotPrice")
    List<ProductPayoff> findByProductAndScenarioTypeOrderBySpotPrice(@Param("product") Product product, @Param("scenarioType") String scenarioType);
    
    @Query("DELETE FROM ProductPayoff pp WHERE pp.product = :product")
    void deleteByProduct(@Param("product") Product product);
}