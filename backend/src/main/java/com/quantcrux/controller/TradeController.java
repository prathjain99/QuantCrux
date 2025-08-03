package com.quantcrux.controller;

import com.quantcrux.dto.*;
import com.quantcrux.model.InstrumentType;
import com.quantcrux.security.UserPrincipal;
import com.quantcrux.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trades")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TradeController {
    
    @Autowired
    private TradeService tradeService;
    
    @GetMapping("/orders")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserOrders(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<OrderResponse> orders = tradeService.getUserOrders(userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve orders: " + e.getMessage()));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserTrades(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<TradeResponse> trades = tradeService.getUserTrades(userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Trades retrieved successfully", trades));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve trades: " + e.getMessage()));
        }
    }
    
    @GetMapping("/positions")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserPositions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<PositionResponse> positions = tradeService.getUserPositions(userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Positions retrieved successfully", positions));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve positions: " + e.getMessage()));
        }
    }
    
    @PostMapping("/orders")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request, 
                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            OrderResponse order = tradeService.createOrder(request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Order created successfully", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to create order: " + e.getMessage()));
        }
    }
    
    @PutMapping("/orders/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            OrderResponse order = tradeService.cancelOrder(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Order cancelled successfully", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to cancel order: " + e.getMessage()));
        }
    }
    
    @GetMapping("/quotes")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMarketQuotes(@RequestParam List<String> symbols) {
        try {
            List<MarketQuoteResponse> quotes = tradeService.getMarketQuotes(symbols);
            return ResponseEntity.ok(new ApiResponse(true, "Market quotes retrieved successfully", quotes));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve market quotes: " + e.getMessage()));
        }
    }
    
    @GetMapping("/quotes/{symbol}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMarketQuote(@PathVariable String symbol, 
                                          @RequestParam(defaultValue = "ASSET") InstrumentType instrumentType) {
        try {
            MarketQuoteResponse quote = tradeService.getMarketQuote(symbol, instrumentType);
            return ResponseEntity.ok(new ApiResponse(true, "Market quote retrieved successfully", quote));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve market quote: " + e.getMessage()));
        }
    }
}