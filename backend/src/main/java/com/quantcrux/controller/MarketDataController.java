package com.quantcrux.controller;

import com.quantcrux.dto.*;
import com.quantcrux.model.AssetType;
import com.quantcrux.model.DataType;
import com.quantcrux.service.MarketDataService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/market-data")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MarketDataController {
    
    @Autowired
    private MarketDataService marketDataService;
    
    @GetMapping("/price/{symbol}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getLivePrice(@PathVariable String symbol,
                                        @RequestParam(defaultValue = "false") Boolean forceRefresh) {
        try {
            MarketDataRequest request = new MarketDataRequest(symbol, DataType.LIVE_PRICE);
            request.setForceRefresh(forceRefresh);
            
            MarketDataResponse response = marketDataService.getMarketData(request);
            return ResponseEntity.ok(new ApiResponse(true, "Live price retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to get live price: " + e.getMessage()));
        }
    }
    
    @GetMapping("/ohlcv/{symbol}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getOHLCVData(@PathVariable String symbol,
                                        @RequestParam(defaultValue = "1d") String timeframe,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                        @RequestParam(defaultValue = "100") Integer limit) {
        try {
            MarketDataRequest request = new MarketDataRequest(symbol, DataType.OHLCV);
            request.setTimeframe(timeframe);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
            request.setLimit(limit);
            
            MarketDataResponse response = marketDataService.getMarketData(request);
            return ResponseEntity.ok(new ApiResponse(true, "OHLCV data retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to get OHLCV data: " + e.getMessage()));
        }
    }
    
    @PostMapping("/batch")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getBatchMarketData(@Valid @RequestBody List<MarketDataRequest> requests) {
        try {
            List<MarketDataResponse> responses = requests.stream()
                    .map(marketDataService::getMarketData)
                    .toList();
            
            return ResponseEntity.ok(new ApiResponse(true, "Batch market data retrieved successfully", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to get batch market data: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<?> searchSymbols(@RequestParam String query) {
        try {
            List<SymbolSearchResponse> results = marketDataService.searchSymbols(query);
            return ResponseEntity.ok(new ApiResponse(true, "Symbol search completed successfully", results));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to search symbols: " + e.getMessage()));
        }
    }
    
    @GetMapping("/popular")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPopularSymbols(@RequestParam(required = false) AssetType assetType) {
        try {
            List<SymbolSearchResponse> symbols = marketDataService.getPopularSymbols(assetType);
            return ResponseEntity.ok(new ApiResponse(true, "Popular symbols retrieved successfully", symbols));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to get popular symbols: " + e.getMessage()));
        }
    }
    
    @GetMapping("/benchmark/{symbol}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getBenchmarkData(@PathVariable String symbol,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            List<MarketDataResponse> data = marketDataService.getBenchmarkData(symbol, startTime, endTime);
            return ResponseEntity.ok(new ApiResponse(true, "Benchmark data retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to get benchmark data: " + e.getMessage()));
        }
    }
    
    @PostMapping("/refresh-cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refreshCache() {
        try {
            marketDataService.refreshCache();
            return ResponseEntity.ok(new ApiResponse(true, "Cache refreshed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to refresh cache: " + e.getMessage()));
        }
    }
}