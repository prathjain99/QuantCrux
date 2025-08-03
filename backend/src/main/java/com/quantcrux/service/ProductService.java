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
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;


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
            
            // Get current market data using the new MarketDataService
            MarketDataResponse marketData = marketDataService.getLivePrice(product.getUnderlyingAsset());
            BigDecimal currentPrice = marketData.getPrice();
            
            if (currentPrice == null) {
                logger.warn("No current price available for {}, using base price", product.getUnderlyingAsset());
                currentPrice = getBasePrice(product.getUnderlyingAsset());
            }
            
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
        // Calculate Greeks using finite differences method
        BigDecimal epsilon = BigDecimal.valueOf(0.01); // 1% bump
        BigDecimal timeEpsilon = BigDecimal.valueOf(1.0 / 365.0); // 1 day
        BigDecimal volEpsilon = BigDecimal.valueOf(0.01); // 1% vol bump
        BigDecimal rateEpsilon = BigDecimal.valueOf(0.0001); // 1bp rate bump
        
        // Get base price
        BigDecimal basePrice = calculateSinglePrice(product, currentPrice, timeToMaturity, request);
        
        // Delta: sensitivity to underlying price (∂V/∂S)
        BigDecimal priceUp = currentPrice.multiply(BigDecimal.ONE.add(epsilon));
        BigDecimal priceDown = currentPrice.multiply(BigDecimal.ONE.subtract(epsilon));
        
        BigDecimal priceUpValue = calculateSinglePrice(product, priceUp, timeToMaturity, request);
        BigDecimal priceDownValue = calculateSinglePrice(product, priceDown, timeToMaturity, request);
        
        BigDecimal delta = priceUpValue.subtract(priceDownValue)
                .divide(priceUp.subtract(priceDown), 6, RoundingMode.HALF_UP);
        result.setDelta(delta);
        
        // Gamma: sensitivity of delta to underlying price (∂²V/∂S²)
        BigDecimal gamma = priceUpValue.add(priceDownValue).subtract(basePrice.multiply(BigDecimal.valueOf(2)))
                .divide(currentPrice.multiply(epsilon).pow(2), 6, RoundingMode.HALF_UP);
        result.setGamma(gamma);
        
        // Theta: sensitivity to time decay (∂V/∂t)
        if (timeToMaturity.compareTo(timeEpsilon) > 0) {
            BigDecimal timeDown = timeToMaturity.subtract(timeEpsilon);
            BigDecimal timeDownValue = calculateSinglePrice(product, currentPrice, timeDown, request);
            BigDecimal theta = timeDownValue.subtract(basePrice).divide(timeEpsilon, 6, RoundingMode.HALF_UP);
            result.setTheta(theta);
        } else {
            result.setTheta(BigDecimal.valueOf(-1.0)); // Default time decay
        }
        
        // Vega: sensitivity to volatility (∂V/∂σ)
        BigDecimal volUp = request.getImpliedVolatility().add(volEpsilon);
        ProductRequest volUpRequest = cloneRequest(request);
        volUpRequest.setImpliedVolatility(volUp);
        
        BigDecimal volUpValue = calculateSinglePrice(product, currentPrice, timeToMaturity, volUpRequest);
        BigDecimal vega = volUpValue.subtract(basePrice).divide(volEpsilon, 6, RoundingMode.HALF_UP);
        result.setVega(vega);
        
        // Rho: sensitivity to interest rate (∂V/∂r)
        BigDecimal rateUp = request.getRiskFreeRate().add(rateEpsilon);
        ProductRequest rateUpRequest = cloneRequest(request);
        rateUpRequest.setRiskFreeRate(rateUp);
        
        BigDecimal rateUpValue = calculateSinglePrice(product, currentPrice, timeToMaturity, rateUpRequest);
        BigDecimal rho = rateUpValue.subtract(basePrice).divide(rateEpsilon, 6, RoundingMode.HALF_UP);
        result.setRho(rho);
    }
    
    private BigDecimal calculateSinglePrice(Product product, BigDecimal currentPrice, BigDecimal timeToMaturity, ProductRequest request) {
        // Simplified single-path pricing for Greeks calculation
        try {
            PricingResult tempResult = calculatePrice(product, currentPrice, timeToMaturity, request);
            return tempResult.getFairValue();
        } catch (Exception e) {
            logger.error("Failed to calculate single price for Greeks", e);
            return product.getNotional().multiply(BigDecimal.valueOf(0.95));
        }
    }
    
    private ProductRequest cloneRequest(ProductRequest original) {
        ProductRequest clone = new ProductRequest();
        clone.setPricingModel(original.getPricingModel());
        clone.setSimulationRuns(Math.min(1000, original.getSimulationRuns())); // Reduce for Greeks calc
        clone.setRiskFreeRate(original.getRiskFreeRate());
        clone.setImpliedVolatility(original.getImpliedVolatility());
        return clone;
    }
    
    private BigDecimal calculateHistoricalVolatility(String symbol) {
        try {
            // Get 30 days of historical data
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(30);
            
            MarketDataResponse historicalData = marketDataService.getOHLCVData(symbol, "1d", startTime, endTime);
            
            if (historicalData.getOhlcvData() != null && historicalData.getOhlcvData().size() > 1) {
                List<BigDecimal> returns = new ArrayList<>();
                List<MarketDataResponse.OHLCVData> ohlcvData = historicalData.getOhlcvData();
                
                for (int i = 1; i < ohlcvData.size(); i++) {
                    BigDecimal prevClose = ohlcvData.get(i - 1).getClose();
                    BigDecimal currentClose = ohlcvData.get(i).getClose();
                    
                    if (prevClose.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal dailyReturn = BigDecimal.valueOf(Math.log(currentClose.divide(prevClose, 10, RoundingMode.HALF_UP).doubleValue()));
                        returns.add(dailyReturn);
                    }
                }
                
                if (returns.size() > 1) {
                    // Calculate standard deviation of log returns
                    BigDecimal mean = returns.stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(returns.size()), 10, RoundingMode.HALF_UP);
                    
                    BigDecimal variance = returns.stream()
                            .map(r -> r.subtract(mean).pow(2))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(returns.size() - 1), 10, RoundingMode.HALF_UP);
                    
                    // Annualize volatility
                    BigDecimal volatility = BigDecimal.valueOf(Math.sqrt(variance.doubleValue() * 252));
                    
                    logger.info("Calculated historical volatility for {}: {}", symbol, volatility);
                    return volatility;
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to calculate historical volatility for {}", symbol, e);
        }
        
        // Fallback to asset-specific default volatilities
        return getDefaultVolatility(symbol);
    }
    
    private BigDecimal getDefaultVolatility(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTCUSD":
            case "ETHUSD":
            case "ADAUSD":
            case "SOLUSD":
                return BigDecimal.valueOf(0.60); // 60% for crypto
            case "TSLA":
                return BigDecimal.valueOf(0.40); // 40% for volatile stocks
            case "SPY":
            case "QQQ":
            case "VTI":
                return BigDecimal.valueOf(0.15); // 15% for ETFs
            default:
                return BigDecimal.valueOf(0.25); // 25% for regular stocks
        }
    }
    
    private boolean isPathDependentProduct(Product product) {
        return product.getProductType() == ProductType.BARRIER_OPTION ||
               product.getProductType() == ProductType.KNOCK_IN_OPTION ||
               product.getProductType() == ProductType.KNOCK_OUT_OPTION;
    }
    
    private double calculateStrategyReturn(Product product, BigDecimal finalPrice, BigDecimal initialPrice) {
        if (product.getLinkedStrategy() != null) {
            // Calculate return based on underlying asset performance
            BigDecimal assetReturn = finalPrice.subtract(initialPrice).divide(initialPrice, 6, RoundingMode.HALF_UP);
            
            // Apply strategy alpha/beta (simplified)
            double alpha = 0.02; // 2% annual alpha
            double beta = 0.8;   // 80% correlation to underlying
            
            return alpha + (beta * assetReturn.doubleValue());
        }
        
        // Fallback to market return
        return finalPrice.subtract(initialPrice).divide(initialPrice, 6, RoundingMode.HALF_UP).doubleValue();
    }
    
    private double calculateCustomPayoff(Product product, BigDecimal finalPrice, BigDecimal initialPrice, List<Double> pricePath) {
        try {
            // Parse custom payoff configuration
            com.fasterxml.jackson.databind.JsonNode config = objectMapper.readTree(product.getConfigJson());
            com.fasterxml.jackson.databind.JsonNode payoffConfig = config.get("custom_payoff");
            
            if (payoffConfig != null) {
                String payoffType = payoffConfig.path("type").asText("linear");
                
                switch (payoffType.toLowerCase()) {
                    case "linear":
                        double slope = payoffConfig.path("slope").asDouble(1.0);
                        double intercept = payoffConfig.path("intercept").asDouble(0.0);
                        return slope * finalPrice.doubleValue() + intercept;
                        
                    case "step":
                        double threshold = payoffConfig.path("threshold").asDouble(initialPrice.doubleValue());
                        double lowPayoff = payoffConfig.path("low_payoff").asDouble(0.0);
                        double highPayoff = payoffConfig.path("high_payoff").asDouble(product.getNotional().doubleValue());
                        return finalPrice.doubleValue() >= threshold ? highPayoff : lowPayoff;
                        
                    case "range":
                        double lowerBound = payoffConfig.path("lower_bound").asDouble(initialPrice.doubleValue() * 0.9);
                        double upperBound = payoffConfig.path("upper_bound").asDouble(initialPrice.doubleValue() * 1.1);
                        if (finalPrice.doubleValue() >= lowerBound && finalPrice.doubleValue() <= upperBound) {
                            return product.getNotional().multiply(product.getPayoffRate() != null ? product.getPayoffRate() : BigDecimal.valueOf(0.1)).doubleValue();
                        }
                        return 0.0;
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to calculate custom payoff", e);
        }
        
        // Default to simple participation
        double participation = product.getPayoffRate() != null ? product.getPayoffRate().doubleValue() : 1.0;
        return Math.max(0, (finalPrice.doubleValue() - initialPrice.doubleValue()) * participation);
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