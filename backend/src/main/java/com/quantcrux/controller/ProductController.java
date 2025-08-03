package com.quantcrux.controller;

import com.quantcrux.dto.*;
import com.quantcrux.model.ProductVersion;
import com.quantcrux.security.UserPrincipal;
import com.quantcrux.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProducts(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<ProductResponse> products = productService.getUserProducts(userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve products: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getProduct(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            ProductResponse product = productService.getProduct(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Product retrieved successfully", product));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve product: " + e.getMessage()));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request, 
                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            ProductResponse product = productService.createProduct(request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Product created successfully", product));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to create product: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest request,
                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            ProductResponse product = productService.updateProduct(id, request, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", product));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update product: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            productService.deleteProduct(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete product: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/issue")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> issueProduct(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            ProductResponse product = productService.issueProduct(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Product issued successfully", product));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to issue product: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/reprice")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> repriceProduct(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            ProductResponse product = productService.repriceProduct(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Product repriced successfully", product));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to reprice product: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/versions")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getProductVersions(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<ProductVersion> versions = productService.getProductVersions(id, userPrincipal);
            return ResponseEntity.ok(new ApiResponse(true, "Product versions retrieved successfully", versions));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve product versions: " + e.getMessage()));
        }
    }
}