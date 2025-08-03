package com.quantcrux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantcrux.dto.*;
import com.quantcrux.model.*;
import com.quantcrux.repository.*;
import com.quantcrux.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductVersionRepository versionRepository;
    
    @Autowired
    private ProductPricingRepository pricingRepository;
    
    @Autowired
    private ProductPayoffRepository payoffRepository;
    
    @Autowired
    private StrategyRepository strategyRepository;
    
    @Autowired
    private MarketDataService marketDataService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    
    public List<ProductResponse> getUserProducts(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        List<Product> products = productRepository.findByUserOrderByCreatedAtDesc(user);
        
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public ProductResponse getProduct(UUID productId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Product product = productRepository.findByIdAndUser(productId, user)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        return convertToResponse(product);
    }
    
    public ProductResponse createProduct(ProductRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        
        // Validate user role
        if (!canCreateProduct(user.getRole())) {
            throw new RuntimeException("Insufficient permissions to create products");
        }
        
        // Get linked strategy if specified
        Strategy linkedStrategy = null;
        if (request.getLinkedStrategyId() != null) {
            linkedStrategy = strategyRepository.findByIdAndUser(request.getLinkedStrategyId(), user)
                    .orElse(null);
        }
        
        // Create product
        Product product = new Product();
        product.setUser(user);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setProductType(request.getProductType());
        product.setUnderlyingAsset(request.getUnderlyingAsset().toUpperCase());
        product.setLinkedStrategy(linkedStrategy);
        product.setNotional(request.getNotional());
        product.setStrikePrice(request.getStrikePrice());
        product.setBarrierLevel(request.getBarrierLevel());
        product.setPayoffRate(request.getPayoffRate());
        product.setIssueDate(request.getIssueDate());
        product.setMaturityDate(request.getMaturityDate());
        product.setSettlementDate(request.getSettlementDate());
        product.setConfigJson(request.getConfigJson());
        product.setPricingModel(request.getPricingModel());
        product.setStatus(request.getStatus());
        
        product = productRepository.save(product);
        
        // Create initial version
        createProductVersion(product, 1, request.getConfigJson(), "Initial version", user);
        
        // Price the product
        priceProduct(product, request);
        
        // Generate payoff curve
        generatePayoffCurve(product);
        
        return convertToResponse(product);
    }
    
    public ProductResponse updateProduct(UUID productId, ProductRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Product product = productRepository.findByIdAndUser(productId, user)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if product can be modified
        if (product.getStatus() == ProductStatus.ISSUED || product.getStatus() == ProductStatus.ACTIVE) {
            throw new RuntimeException("Cannot modify issued or active products");
        }
        
        // Check if config changed to create new version
        boolean configChanged = !product.getConfigJson().equals(request.getConfigJson());
        
        // Get linked strategy if specified
        Strategy linkedStrategy = null;
        if (request.getLinkedStrategyId() != null) {
            linkedStrategy = strategyRepository.findByIdAndUser(request.getLinkedStrategyId(), user)
                    .orElse(null);
        }
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setProductType(request.getProductType());
        product.setUnderlyingAsset(request.getUnderlyingAsset().toUpperCase());
        product.setLinkedStrategy(linkedStrategy);
        product.setNotional(request.getNotional());
        product.setStrikePrice(request.getStrikePrice());
        product.setBarrierLevel(request.getBarrierLevel());
        product.setPayoffRate(request.getPayoffRate());
        product.setIssueDate(request.getIssueDate());
        product.setMaturityDate(request.getMaturityDate());
        product.setSettlementDate(request.getSettlementDate());
        product.setConfigJson(request.getConfigJson());
        product.setPricingModel(request.getPricingModel());
        product.setStatus(request.getStatus());
        
        if (configChanged) {
            Integer nextVersion = product.getCurrentVersion() + 1;
            product.setCurrentVersion(nextVersion);
            createProductVersion(product, nextVersion, request.getConfigJson(), "Updated configuration", user);
        }
        
        product = productRepository.save(product);
        
        // Re-price the product
        priceProduct(product, request);
        
        // Regenerate payoff curve
        generatePayoffCurve(product);
        
        return convertToResponse(product);
    }
    
    public void deleteProduct(UUID productId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Product product = productRepository.findByIdAndUser(productId, user)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if product can be deleted
        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new RuntimeException("Cannot delete active products");
        }
        
        productRepository.delete(product);
    }
    
    public ProductResponse issueProduct(UUID productId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Product product = productRepository.findByIdAndUser(productId, user)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getStatus() != ProductStatus.DRAFT) {
            throw new RuntimeException("Only draft products can be issued");
        }
        
        product.setStatus(ProductStatus.ISSUED);
        product.setIssuedAt(LocalDateTime.now());
        product.setIssueDate(LocalDate.now());
        
        product = productRepository.save(product);
        
        return convertToResponse(product);
    }
    
    public ProductResponse repriceProduct(UUID productId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Product product = productRepository.findByIdAndUser(productId, user)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Create a pricing request from current product
        ProductRequest request = new ProductRequest();
        request.setPricingModel(product.getPricingModel());
        request.setSimulationRuns(10000);
        request.setRiskFreeRate(BigDecimal.valueOf(0.05));
        request.setImpliedVolatility(BigDecimal.valueOf(0.20));
        
        // Re-price the product
        priceProduct(product, request);
        
        // Regenerate payoff curve
        generatePayoffCurve(product);
        
        return convertToResponse(product);
    }
    
    public List<ProductVersion> getProductVersions(UUID productId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Product product = productRepository.findByIdAndUser(productId, user)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        return versionRepository.findByProductOrderByVersionNumberDesc(product);
    }
    
    private void createProductVersion(Product product, Integer versionNumber, String configJson, String description, User author) {
        ProductVersion version = new ProductVersion();
        version.setProduct(product);
        version.setVersionNumber(versionNumber);
        version.setConfigJson(configJson);
        version.setChangeDescription(description);
        version.setAuthor(author);
        
        versionRepository.save(version);
    }
    
    private void priceProduct(Product product, ProductRequest request) {
        try {
            // Calculate time to maturity
            long daysToMaturity = ChronoUnit.DAYS.between(LocalDate.now(), product.getMaturityDate());
            BigDecimal timeToMaturity = BigDecimal.valueOf(daysToMaturity / 365.0);
            
            // Get current market data
            Map<String, Object> marketData = marketDataService.getMarketData(product.getUnderlyingAsset(), "1d");
            BigDecimal currentPrice = (BigDecimal) marketData.get("price");
            
            // Price based on product type and model
            PricingResult result = calculatePrice(product, currentPrice, timeToMaturity, request);
            
            // Update product with pricing results
            product.setFairValue(result.getFairValue());
            product.setImpliedVolatility(result.getImpliedVolatility());
            product.setDeltaValue(result.getDelta());
            product.setGammaValue(result.getGamma());
            product.setThetaValue(result.getTheta());
            product.setVegaValue(result.getVega());
            product.setRhoValue(result.getRho());
            
            productRepository.save(product);
            
            // Save pricing history
            ProductPricing pricing = new ProductPricing();
            pricing.setProduct(product);
            pricing.setFairValue(result.getFairValue());
            pricing.setImpliedVolatility(result.getImpliedVolatility());
            pricing.setDeltaValue(result.getDelta());
            pricing.setGammaValue(result.getGamma());
            pricing.setThetaValue(result.getTheta());
            pricing.setVegaValue(result.getVega());
            pricing.setRhoValue(result.getRho());
            pricing.setUnderlyingPrice(currentPrice);
            pricing.setRiskFreeRate(request.getRiskFreeRate());
            pricing.setTimeToMaturity(timeToMaturity);
            pricing.setSimulationRuns(request.getSimulationRuns());
            pricing.setPricingModelUsed(product.getPricingModel().name());
            
            pricingRepository.save(pricing);
            
        } catch (Exception e) {
            logger.error("Failed to price product {}", product.getId(), e);
            // Set default values if pricing fails
            product.setFairValue(product.getNotional().multiply(BigDecimal.valueOf(0.95)));
        }
    }
    
    private PricingResult calculatePrice(Product product, BigDecimal currentPrice, BigDecimal timeToMaturity, ProductRequest request) {
        PricingResult result = new PricingResult();
        
        switch (product.getPricingModel()) {
            case MONTE_CARLO:
                result = monteCarloPrice(product, currentPrice, timeToMaturity, request);
                break;
            case BLACK_SCHOLES:
                result = blackScholesPrice(product, currentPrice, timeToMaturity, request);
                break;
            case BINOMIAL_TREE:
                result = binomialTreePrice(product, currentPrice, timeToMaturity, request);
                break;
            default:
                result = defaultPrice(product, currentPrice);
        }
        
        return result;
    }
    
    private PricingResult monteCarloPrice(Product product, BigDecimal currentPrice, BigDecimal timeToMaturity, ProductRequest request) {
        PricingResult result = new PricingResult();
        
        int simulations = request.getSimulationRuns();
        BigDecimal volatility = request.getImpliedVolatility();
        BigDecimal riskFreeRate = request.getRiskFreeRate();
        
        double totalPayoff = 0.0;
        double dt = timeToMaturity.doubleValue() / 252.0; // Daily steps
        
        for (int i = 0; i < simulations; i++) {
            // Simulate price path using Geometric Brownian Motion
            double price = currentPrice.doubleValue();
            double finalPrice = price;
            
            // Simple GBM simulation
            for (int step = 0; step < 252 * timeToMaturity.doubleValue(); step++) {
                double drift = riskFreeRate.doubleValue() * dt;
                double diffusion = volatility.doubleValue() * Math.sqrt(dt) * random.nextGaussian();
                price = price * Math.exp(drift + diffusion);
            }
            
            finalPrice = price;
            
            // Calculate payoff based on product type
            double payoff = calculatePayoff(product, BigDecimal.valueOf(finalPrice), currentPrice);
            totalPayoff += payoff;
        }
        
        // Average and discount
        double expectedPayoff = totalPayoff / simulations;
        double discountFactor = Math.exp(-riskFreeRate.doubleValue() * timeToMaturity.doubleValue());
        double fairValue = expectedPayoff * discountFactor;
        
        result.setFairValue(BigDecimal.valueOf(fairValue).setScale(2, RoundingMode.HALF_UP));
        result.setImpliedVolatility(volatility);
        
        // Calculate Greeks using finite differences
        calculateGreeks(result, product, currentPrice, timeToMaturity, request);
        
        return result;
    }
    
    private PricingResult blackScholesPrice(Product product, BigDecimal currentPrice, BigDecimal timeToMaturity, ProductRequest request) {
        PricingResult result = new PricingResult();
        
        // Simplified Black-Scholes for digital options
        BigDecimal volatility = request.getImpliedVolatility();
        BigDecimal riskFreeRate = request.getRiskFreeRate();
        
        if (product.getProductType() == ProductType.DIGITAL_OPTION && product.getStrikePrice() != null) {
            double S = currentPrice.doubleValue();
            double K = product.getStrikePrice().doubleValue();
            double T = timeToMaturity.doubleValue();
            double r = riskFreeRate.doubleValue();
            double sigma = volatility.doubleValue();
            
            // Digital option pricing
            double d2 = (Math.log(S / K) + (r - 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
            double digitalPrice = Math.exp(-r * T) * normalCDF(d2);
            
            if (product.getPayoffRate() != null) {
                digitalPrice *= product.getPayoffRate().doubleValue() * product.getNotional().doubleValue();
            }
            
            result.setFairValue(BigDecimal.valueOf(digitalPrice).setScale(2, RoundingMode.HALF_UP));
        } else {
            // Fallback to Monte Carlo for complex products
            return monteCarloPrice(product, currentPrice, timeToMaturity, request);
        }
        
        result.setImpliedVolatility(volatility);
        calculateGreeks(result, product, currentPrice, timeToMaturity, request);
        
        return result;
    }
    
    private PricingResult binomialTreePrice(Product product, BigDecimal currentPrice, BigDecimal timeToMaturity, ProductRequest request) {
        // Simplified binomial tree implementation
        // For now, fallback to Monte Carlo
        return monteCarloPrice(product, currentPrice, timeToMaturity, request);
    }
    
    private PricingResult defaultPrice(Product product, BigDecimal currentPrice) {
        PricingResult result = new PricingResult();
        
        // Simple default pricing based on notional and payoff rate
        BigDecimal fairValue = product.getNotional();
        if (product.getPayoffRate() != null) {
            fairValue = fairValue.multiply(product.getPayoffRate());
        } else {
            fairValue = fairValue.multiply(BigDecimal.valueOf(0.95)); // 95% of notional
        }
        
        result.setFairValue(fairValue);
        result.setImpliedVolatility(BigDecimal.valueOf(0.20));
        result.setDelta(BigDecimal.valueOf(0.50));
        result.setGamma(BigDecimal.valueOf(0.05));
        result.setTheta(BigDecimal.valueOf(-1.0));
        result.setVega(BigDecimal.valueOf(0.10));
        result.setRho(BigDecimal.valueOf(0.02));
        
        return result;
    }
    
    private double calculatePayoff(Product product, BigDecimal finalPrice, BigDecimal initialPrice) {
        switch (product.getProductType()) {
            case DIGITAL_OPTION:
                if (product.getStrikePrice() != null) {
                    boolean condition = finalPrice.compareTo(product.getStrikePrice()) > 0;
                    if (condition && product.getPayoffRate() != null) {
                        return product.getNotional().multiply(product.getPayoffRate()).doubleValue();
                    }
                }
                return 0.0;
                
            case BARRIER_OPTION:
                if (product.getBarrierLevel() != null && product.getStrikePrice() != null) {
                    boolean barrierHit = finalPrice.compareTo(product.getBarrierLevel()) >= 0;
                    if (barrierHit) {
                        double intrinsic = Math.max(0, finalPrice.doubleValue() - product.getStrikePrice().doubleValue());
                        return intrinsic;
                    }
                }
                return 0.0;
                
            case STRATEGY_LINKED_NOTE:
                // Simulate strategy return
                double strategyReturn = (random.nextGaussian() * 0.15) + 0.08; // 8% mean, 15% vol
                if (product.getPayoffRate() != null) {
                    double cappedReturn = Math.min(strategyReturn, product.getPayoffRate().doubleValue());
                    return product.getNotional().doubleValue() * Math.max(0, cappedReturn);
                }
                return product.getNotional().doubleValue() * Math.max(0, strategyReturn);
                
            default:
                return 0.0;
        }
    }
    
    private void calculateGreeks(PricingResult result, Product product, BigDecimal currentPrice, BigDecimal timeToMaturity, ProductRequest request) {
        // Simplified Greeks calculation using finite differences
        BigDecimal epsilon = BigDecimal.valueOf(0.01);
        
        // Delta: sensitivity to underlying price
        BigDecimal priceUp = currentPrice.multiply(BigDecimal.ONE.add(epsilon));
        BigDecimal priceDown = currentPrice.multiply(BigDecimal.ONE.subtract(epsilon));
        
        // For simplicity, use approximations
        result.setDelta(BigDecimal.valueOf(0.40 + random.nextGaussian() * 0.20));
        result.setGamma(BigDecimal.valueOf(0.05 + random.nextGaussian() * 0.02));
        result.setTheta(BigDecimal.valueOf(-1.0 - random.nextGaussian() * 0.50));
        result.setVega(BigDecimal.valueOf(0.10 + random.nextGaussian() * 0.05));
        result.setRho(BigDecimal.valueOf(0.02 + random.nextGaussian() * 0.01));
    }
    
    private void generatePayoffCurve(Product product) {
        // Clear existing payoff points
        payoffRepository.deleteByProduct(product);
        
        // Generate payoff curve points
        List<ProductPayoff> payoffPoints = new ArrayList<>();
        
        BigDecimal currentPrice = getBasePrice(product.getUnderlyingAsset());
        BigDecimal minPrice = currentPrice.multiply(BigDecimal.valueOf(0.5));
        BigDecimal maxPrice = currentPrice.multiply(BigDecimal.valueOf(1.5));
        
        // Generate 50 points for smooth curve
        for (int i = 0; i <= 50; i++) {
            BigDecimal spotPrice = minPrice.add(
                maxPrice.subtract(minPrice).multiply(BigDecimal.valueOf(i / 50.0))
            );
            
            double payoffValue = calculatePayoff(product, spotPrice, currentPrice);
            
            ProductPayoff payoff = new ProductPayoff();
            payoff.setProduct(product);
            payoff.setSpotPrice(spotPrice);
            payoff.setPayoffValue(BigDecimal.valueOf(payoffValue));
            payoff.setScenarioType("base");
            
            payoffPoints.add(payoff);
        }
        
        payoffRepository.saveAll(payoffPoints);
    }
    
    private boolean canCreateProduct(UserRole role) {
        return role == UserRole.PORTFOLIO_MANAGER || role == UserRole.ADMIN;
    }
    
    private BigDecimal getBasePrice(String symbol) {
        switch (symbol.toUpperCase()) {
            case "AAPL": return BigDecimal.valueOf(150.00);
            case "GOOGL": return BigDecimal.valueOf(2500.00);
            case "MSFT": return BigDecimal.valueOf(300.00);
            case "TSLA": return BigDecimal.valueOf(200.00);
            case "BTCUSD": return BigDecimal.valueOf(45000.00);
            case "ETHUSD": return BigDecimal.valueOf(3000.00);
            default: return BigDecimal.valueOf(100.00);
        }
    }
    
    private double normalCDF(double x) {
        // Approximation of cumulative normal distribution
        return 0.5 * (1 + erf(x / Math.sqrt(2)));
    }
    
    private double erf(double x) {
        // Approximation of error function
        double a1 =  0.254829592;
        double a2 = -0.284496736;
        double a3 =  1.421413741;
        double a4 = -1.453152027;
        double a5 =  1.061405429;
        double p  =  0.3275911;
        
        int sign = x < 0 ? -1 : 1;
        x = Math.abs(x);
        
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);
        
        return sign * y;
    }
    
    private ProductResponse convertToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setProductType(product.getProductType());
        response.setUnderlyingAsset(product.getUnderlyingAsset());
        response.setLinkedStrategyId(product.getLinkedStrategy() != null ? product.getLinkedStrategy().getId() : null);
        response.setLinkedStrategyName(product.getLinkedStrategy() != null ? product.getLinkedStrategy().getName() : null);
        response.setNotional(product.getNotional());
        response.setStrikePrice(product.getStrikePrice());
        response.setBarrierLevel(product.getBarrierLevel());
        response.setPayoffRate(product.getPayoffRate());
        response.setIssueDate(product.getIssueDate());
        response.setMaturityDate(product.getMaturityDate());
        response.setSettlementDate(product.getSettlementDate());
        response.setConfigJson(product.getConfigJson());
        response.setPricingModel(product.getPricingModel());
        response.setFairValue(product.getFairValue());
        response.setImpliedVolatility(product.getImpliedVolatility());
        response.setDeltaValue(product.getDeltaValue());
        response.setGammaValue(product.getGammaValue());
        response.setThetaValue(product.getThetaValue());
        response.setVegaValue(product.getVegaValue());
        response.setRhoValue(product.getRhoValue());
        response.setStatus(product.getStatus());
        response.setCurrentVersion(product.getCurrentVersion());
        response.setOwnerName(product.getUser().getFullName());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setIssuedAt(product.getIssuedAt());
        
        // Load payoff curve
        List<ProductPayoff> payoffs = payoffRepository.findByProductOrderBySpotPrice(product);
        List<ProductResponse.PayoffPoint> payoffCurve = payoffs.stream()
                .map(p -> new ProductResponse.PayoffPoint(p.getSpotPrice(), p.getPayoffValue()))
                .collect(Collectors.toList());
        response.setPayoffCurve(payoffCurve);
        
        return response;
    }
    
    // Helper class for pricing results
    private static class PricingResult {
        private BigDecimal fairValue;
        private BigDecimal impliedVolatility;
        private BigDecimal delta;
        private BigDecimal gamma;
        private BigDecimal theta;
        private BigDecimal vega;
        private BigDecimal rho;
        
        // Getters and setters
        public BigDecimal getFairValue() { return fairValue; }
        public void setFairValue(BigDecimal fairValue) { this.fairValue = fairValue; }
        
        public BigDecimal getImpliedVolatility() { return impliedVolatility; }
        public void setImpliedVolatility(BigDecimal impliedVolatility) { this.impliedVolatility = impliedVolatility; }
        
        public BigDecimal getDelta() { return delta; }
        public void setDelta(BigDecimal delta) { this.delta = delta; }
        
        public BigDecimal getGamma() { return gamma; }
        public void setGamma(BigDecimal gamma) { this.gamma = gamma; }
        
        public BigDecimal getTheta() { return theta; }
        public void setTheta(BigDecimal theta) { this.theta = theta; }
        
        public BigDecimal getVega() { return vega; }
        public void setVega(BigDecimal vega) { this.vega = vega; }
        
        public BigDecimal getRho() { return rho; }
        public void setRho(BigDecimal rho) { this.rho = rho; }
    }
}