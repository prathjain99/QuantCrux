package com.quantcrux.repository;

import com.quantcrux.model.AssetType;
import com.quantcrux.model.SymbolMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SymbolMetadataRepository extends JpaRepository<SymbolMetadata, UUID> {
    
    Optional<SymbolMetadata> findBySymbol(String symbol);
    
    List<SymbolMetadata> findByAssetType(AssetType assetType);
    
    List<SymbolMetadata> findByExchange(String exchange);
    
    List<SymbolMetadata> findBySector(String sector);
    
    @Query("SELECT sm FROM SymbolMetadata sm WHERE sm.symbol ILIKE %:query% OR sm.name ILIKE %:query% ORDER BY sm.symbol")
    List<SymbolMetadata> searchBySymbolOrName(@Param("query") String query);
    
    @Query("SELECT sm FROM SymbolMetadata sm WHERE sm.isTradeable = true ORDER BY sm.marketCap DESC NULLS LAST")
    List<SymbolMetadata> findTradeableOrderByMarketCap();
    
    @Query("SELECT sm FROM SymbolMetadata sm WHERE sm.assetType = :assetType AND sm.isTradeable = true ORDER BY sm.marketCap DESC NULLS LAST")
    List<SymbolMetadata> findByAssetTypeAndTradeableOrderByMarketCap(@Param("assetType") AssetType assetType);
    
    @Query("SELECT sm FROM SymbolMetadata sm WHERE sm.sector = :sector AND sm.isTradeable = true ORDER BY sm.marketCap DESC NULLS LAST")
    List<SymbolMetadata> findBySectorAndTradeableOrderByMarketCap(@Param("sector") String sector);
    
    @Query("SELECT DISTINCT sm.sector FROM SymbolMetadata sm WHERE sm.sector IS NOT NULL ORDER BY sm.sector")
    List<String> findDistinctSectors();
    
    @Query("SELECT DISTINCT sm.exchange FROM SymbolMetadata sm WHERE sm.exchange IS NOT NULL ORDER BY sm.exchange")
    List<String> findDistinctExchanges();
    
    @Query("SELECT COUNT(sm) FROM SymbolMetadata sm WHERE sm.assetType = :assetType AND sm.isTradeable = true")
    long countByAssetTypeAndTradeable(@Param("assetType") AssetType assetType);
}