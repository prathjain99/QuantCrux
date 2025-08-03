package com.quantcrux.controller;

import com.quantcrux.dto.*;
import com.quantcrux.security.UserPrincipal;
import com.quantcrux.service.BacktestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/backtests")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BacktestController {
    
    @Autowired
    private BacktestService backtestService;
    
    @GetMapping
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserBacktests(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<BacktestResponse> backtests = backtestService.getUserBacktests(userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Backtests retrieved successfully", backtests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve backtests: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getBacktest(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            BacktestResponse backtest = backtestService.getBacktest(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Backtest retrieved successfully", backtest));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve backtest: " + e.getMessage()));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createBacktest(@Valid @RequestBody BacktestRequest request, 
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            BacktestResponse backtest = backtestService.createBacktest(request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Backtest created successfully", backtest));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to create backtest: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteBacktest(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            backtestService.deleteBacktest(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Backtest deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete backtest: " + e.getMessage()));
        }
    }
    
    @GetMapping("/strategy/{strategyId}")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getStrategyBacktests(@PathVariable UUID strategyId, 
                                                @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<BacktestResponse> backtests = backtestService.getStrategyBacktests(strategyId, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Strategy backtests retrieved successfully", backtests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve strategy backtests: " + e.getMessage()));
        }
    }
}