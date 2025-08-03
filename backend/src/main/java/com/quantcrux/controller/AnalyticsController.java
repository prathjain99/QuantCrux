package com.quantcrux.controller;

import com.quantcrux.dto.*;
import com.quantcrux.security.UserPrincipal;
import com.quantcrux.service.AnalyticsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @PostMapping("/portfolio/{portfolioId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPortfolioAnalytics(@PathVariable UUID portfolioId, 
                                                  @Valid @RequestBody AnalyticsRequest request,
                                                  @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            AnalyticsResponse analytics = analyticsService.getPortfolioAnalytics(portfolioId, request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Portfolio analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve portfolio analytics: " + e.getMessage()));
        }
    }
    
    @PostMapping("/strategy/{strategyId}")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getStrategyAnalytics(@PathVariable UUID strategyId, 
                                                 @Valid @RequestBody AnalyticsRequest request,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            AnalyticsResponse analytics = analyticsService.getStrategyAnalytics(strategyId, request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Strategy analytics retrieved successfully", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve strategy analytics: " + e.getMessage()));
        }
    }
    
    @PostMapping("/reports")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> generateReport(@Valid @RequestBody ReportRequest request,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            ReportResponse report = analyticsService.generateReport(request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Report generation started", report));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to generate report: " + e.getMessage()));
        }
    }
    
    @GetMapping("/reports")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserReports(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<ReportResponse> reports = analyticsService.getUserReports(userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Reports retrieved successfully", reports));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve reports: " + e.getMessage()));
        }
    }
    
    @GetMapping("/reports/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getReport(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            ReportResponse report = analyticsService.getReport(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Report retrieved successfully", report));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve report: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/reports/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteReport(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            analyticsService.deleteReport(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Report deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete report: " + e.getMessage()));
        }
    }
}