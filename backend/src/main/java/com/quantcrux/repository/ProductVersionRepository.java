package com.quantcrux.repository;

import com.quantcrux.model.Product;
import com.quantcrux.model.ProductVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVersionRepository extends JpaRepository<ProductVersion, UUID> {
    
    List<ProductVersion> findByProductOrderByVersionNumberDesc(Product product);
    
    Optional<ProductVersion> findByProductAndVersionNumber(Product product, Integer versionNumber);
    
    @Query("SELECT MAX(pv.versionNumber) FROM ProductVersion pv WHERE pv.product = :product")
    Optional<Integer> findMaxVersionNumberByProduct(@Param("product") Product product);
    
    @Query("SELECT pv FROM ProductVersion pv WHERE pv.product = :product ORDER BY pv.createdAt DESC")
    List<ProductVersion> findByProductOrderByCreatedAtDesc(@Param("product") Product product);
}