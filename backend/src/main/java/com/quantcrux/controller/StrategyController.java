package com.quantcrux.controller;

import com.quantcrux.dto.*;
import com.quantcrux.model.StrategyVersion;
import com.quantcrux.security.UserPrincipal;
import com.quantcrux.service.StrategyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/strategies")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StrategyController {
    
    @Autowired
    private StrategyService strategyService;
    
    @GetMapping
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserStrategies(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<StrategyResponse> strategies = strategyService.getUserStrategies(userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Strategies retrieved successfully", strategies));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve strategies: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getStrategy(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            StrategyResponse strategy = strategyService.getStrategy(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Strategy retrieved successfully", strategy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve strategy: " + e.getMessage()));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createStrategy(@Valid @RequestBody StrategyRequest request, 
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            StrategyResponse strategy = strategyService.createStrategy(request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Strategy created successfully", strategy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to create strategy: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateStrategy(@PathVariable UUID id, @Valid @RequestBody StrategyRequest request,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            StrategyResponse strategy = strategyService.updateStrategy(id, request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Strategy updated successfully", strategy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update strategy: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteStrategy(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            strategyService.deleteStrategy(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Strategy deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete strategy: " + e.getMessage()));
        }
    }
    
    @PostMapping("/evaluate")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> evaluateStrategy(@Valid @RequestBody SignalEvaluationRequest request,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            SignalEvaluationResponse evaluation = strategyService.evaluateStrategy(request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Strategy evaluated successfully", evaluation));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to evaluate strategy: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/versions")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getStrategyVersions(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<StrategyVersion> versions = strategyService.getStrategyVersions(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Strategy versions retrieved successfully", versions));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve strategy versions: " + e.getMessage()));
        }
    }
}