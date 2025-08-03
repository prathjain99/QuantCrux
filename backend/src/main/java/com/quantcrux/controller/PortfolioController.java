package com.quantcrux.controller;

import com.quantcrux.dto.*;
import com.quantcrux.security.UserPrincipal;
import com.quantcrux.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/portfolios")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PortfolioController {
    
    @Autowired
    private PortfolioService portfolioService;
    
    @GetMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserPortfolios(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<PortfolioResponse> portfolios = portfolioService.getUserPortfolios(userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Portfolios retrieved successfully", portfolios));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve portfolios: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPortfolio(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            PortfolioResponse portfolio = portfolioService.getPortfolio(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Portfolio retrieved successfully", portfolio));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve portfolio: " + e.getMessage()));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createPortfolio(@Valid @RequestBody PortfolioRequest request, 
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            PortfolioResponse portfolio = portfolioService.createPortfolio(request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Portfolio created successfully", portfolio));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to create portfolio: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updatePortfolio(@PathVariable UUID id, @Valid @RequestBody PortfolioRequest request,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            PortfolioResponse portfolio = portfolioService.updatePortfolio(id, request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Portfolio updated successfully", portfolio));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update portfolio: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deletePortfolio(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            portfolioService.deletePortfolio(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Portfolio deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete portfolio: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/refresh")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> refreshPortfolioMetrics(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            PortfolioResponse portfolio = portfolioService.updatePortfolioMetrics(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Portfolio metrics updated successfully", portfolio));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update portfolio metrics: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/nav")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPortfolioNAVHistory(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<PortfolioResponse.NAVPoint> navHistory = portfolioService.getPortfolioNAVHistory(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "NAV history retrieved successfully", navHistory));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve NAV history: " + e.getMessage()));
        }
    }
}