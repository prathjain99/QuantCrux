package com.quantcrux.service;

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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TradeService {
    
    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TradeRepository tradeRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private MarketQuoteRepository marketQuoteRepository;
    
    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private MarketDataService marketDataService;
    
    public List<OrderResponse> getUserOrders(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        List<Order> orders = orderRepository.findByUserOrManagedPortfoliosOrderByCreatedAtDesc(user);
        
        return orders.stream()
                .map(this::convertOrderToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TradeResponse> getUserTrades(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        List<Trade> trades = tradeRepository.findByUserOrManagedPortfoliosOrderByExecutedAtDesc(user);
        
        return trades.stream()
                .map(this::convertTradeToResponse)
                .collect(Collectors.toList());
    }
    
    public List<PositionResponse> getUserPositions(UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        List<Position> positions = positionRepository.findByUserPortfoliosOrderByUpdatedAtDesc(user);
        
        return positions.stream()
                .map(this::convertPositionToResponse)
                .collect(Collectors.toList());
    }
    
    public OrderResponse createOrder(OrderRequest request, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        
        // Validate user can trade for this portfolio
        Portfolio portfolio = portfolioRepository.findByIdAndUser(request.getPortfolioId(), user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found or access denied"));
        
        // Validate order parameters
        validateOrder(request, portfolio);
        
        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setPortfolio(portfolio);
        order.setInstrumentId(request.getInstrumentId());
        order.setInstrumentType(request.getInstrumentType());
        order.setSymbol(request.getSymbol().toUpperCase());
        order.setSide(request.getSide());
        order.setOrderType(request.getOrderType());
        order.setQuantity(request.getQuantity());
        order.setLimitPrice(request.getLimitPrice());
        order.setStopPrice(request.getStopPrice());
        order.setTimeInForce(request.getTimeInForce());
        order.setExpiresAt(request.getExpiresAt());
        order.setNotes(request.getNotes());
        order.setClientOrderId(request.getClientOrderId());
        order.setSubmittedAt(LocalDateTime.now());
        
        order = orderRepository.save(order);
        
        // Execute order immediately for market orders
        if (request.getOrderType() == OrderType.MARKET) {
            executeOrder(order);
        }
        
        return convertOrderToResponse(order);
    }
    
    public OrderResponse cancelOrder(UUID orderId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Validate user can cancel this order
        if (!order.getUser().getId().equals(user.getId()) && 
            (order.getPortfolio().getManager() == null || !order.getPortfolio().getManager().getId().equals(user.getId()))) {
            throw new RuntimeException("Access denied");
        }
        
        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.FILLED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        
        order = orderRepository.save(order);
        return convertOrderToResponse(order);
    }
    
    public List<MarketQuoteResponse> getMarketQuotes(List<String> symbols) {
        List<MarketQuoteResponse> quotes = new ArrayList<>();
        
        for (String symbol : symbols) {
            try {
                MarketDataResponse marketData = marketDataService.getLivePrice(symbol);
                MarketQuoteResponse quote = convertMarketDataToQuote(marketData);
                quotes.add(quote);
            } catch (Exception e) {
                logger.error("Failed to get market data for symbol: {}", symbol, e);
                // Add a mock quote as fallback
                MarketQuoteResponse mockQuote = generateMockQuoteResponse(symbol);
                quotes.add(mockQuote);
            }
        }
        
        return quotes;
    }
    
    public MarketQuoteResponse getMarketQuote(String symbol, InstrumentType instrumentType) {
        try {
            MarketDataResponse marketData = marketDataService.getLivePrice(symbol);
            return convertMarketDataToQuote(marketData);
        } catch (Exception e) {
            logger.error("Failed to get market data for symbol: {}", symbol, e);
            return generateMockQuoteResponse(symbol);
        }
    }
    
    private void validateOrder(OrderRequest request, Portfolio portfolio) {
        // Validate quantity
        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }
        
        // Validate limit price for limit orders
        if (request.getOrderType() == OrderType.LIMIT && request.getLimitPrice() == null) {
            throw new RuntimeException("Limit price required for limit orders");
        }
        
        // Validate stop price for stop orders
        if ((request.getOrderType() == OrderType.STOP || request.getOrderType() == OrderType.STOP_LIMIT) 
            && request.getStopPrice() == null) {
            throw new RuntimeException("Stop price required for stop orders");
        }
        
        // Check available cash for buy orders
        if (request.getSide() == OrderSide.BUY) {
            BigDecimal estimatedCost = estimateOrderCost(request);
            if (portfolio.getCashBalance().compareTo(estimatedCost) < 0) {
                throw new RuntimeException("Insufficient cash balance. Available: " + 
                    portfolio.getCashBalance() + ", Required: " + estimatedCost);
            }
        }
        
        // Check position for sell orders
        if (request.getSide() == OrderSide.SELL) {
            Optional<Position> position = positionRepository.findByPortfolioAndSymbolAndInstrumentType(
                portfolio, request.getSymbol(), request.getInstrumentType());
            
            if (position.isEmpty() || position.get().getNetQuantity().compareTo(request.getQuantity()) < 0) {
                throw new RuntimeException("Insufficient position to sell. Available: " + 
                    (position.isPresent() ? position.get().getNetQuantity() : BigDecimal.ZERO));
            }
        }
    }
    
    private BigDecimal estimateOrderCost(OrderRequest request) {
        BigDecimal price;
        
        if (request.getOrderType() == OrderType.MARKET) {
            // Get current market data
            MarketDataResponse marketData = marketDataService.getLivePrice(request.getSymbol());
            price = request.getSide() == OrderSide.BUY ? marketData.getAskPrice() : marketData.getBidPrice();
            if (price == null) {
                price = marketData.getPrice();
            }
        } else if (request.getLimitPrice() != null) {
            price = request.getLimitPrice();
        } else {
            // Fallback to last price
            MarketDataResponse marketData = marketDataService.getLivePrice(request.getSymbol());
            price = marketData.getPrice();
        }
        
        if (price == null) {
            throw new RuntimeException("Unable to determine price for symbol: " + request.getSymbol());
        }
        
        BigDecimal totalCost = request.getQuantity().multiply(price);
        BigDecimal fees = totalCost.multiply(BigDecimal.valueOf(0.001)); // 0.1% commission
        
        return totalCost.add(fees);
    }
    
    private void executeOrder(Order order) {
        try {
            logger.info("Executing order {}", order.getId());
            
            // Get real-time market data for execution
            MarketDataResponse marketData = marketDataService.getLivePrice(order.getSymbol());
            
            BigDecimal executionPrice;
            
            // Determine execution price based on order type
            switch (order.getOrderType()) {
                case MARKET:
                    executionPrice = order.getSide() == OrderSide.BUY ? 
                        marketData.getAskPrice() : marketData.getBidPrice();
                    if (executionPrice == null) {
                        executionPrice = marketData.getPrice();
                    }
                    break;
                    
                case LIMIT:
                    // For limit orders, check if limit price can be filled
                    BigDecimal marketPrice = order.getSide() == OrderSide.BUY ? 
                        marketData.getAskPrice() : marketData.getBidPrice();
                    if (marketPrice == null) {
                        marketPrice = marketData.getPrice();
                    }
                    
                    boolean canFill = order.getSide() == OrderSide.BUY ? 
                        order.getLimitPrice().compareTo(marketPrice) >= 0 :
                        order.getLimitPrice().compareTo(marketPrice) <= 0;
                    
                    if (!canFill) {
                        // Order remains pending
                        order.setStatus(OrderStatus.SUBMITTED);
                        orderRepository.save(order);
                        return;
                    }
                    
                    executionPrice = order.getLimitPrice();
                    break;
                    
                case STOP:
                    // Stop orders convert to market orders when triggered
                    boolean triggered = order.getSide() == OrderSide.BUY ?
                        marketData.getPrice().compareTo(order.getStopPrice()) >= 0 :
                        marketData.getPrice().compareTo(order.getStopPrice()) <= 0;
                    
                    if (!triggered) {
                        order.setStatus(OrderStatus.SUBMITTED);
                        orderRepository.save(order);
                        return;
                    }
                    
                    executionPrice = order.getSide() == OrderSide.BUY ? 
                        marketData.getAskPrice() : marketData.getBidPrice();
                    if (executionPrice == null) {
                        executionPrice = marketData.getPrice();
                    }
                    break;
                    
                default:
                    throw new RuntimeException("Unsupported order type: " + order.getOrderType());
            }
            
            if (executionPrice == null) {
                throw new RuntimeException("No execution price available for symbol: " + order.getSymbol());
            }
            
            // Apply realistic slippage based on market conditions
            BigDecimal slippage = calculateSlippage(order, marketData);
            BigDecimal slippageAdjustedPrice = order.getSide() == OrderSide.BUY ?
                executionPrice.add(executionPrice.multiply(slippage)) :
                executionPrice.subtract(executionPrice.multiply(slippage));
            
            // Calculate fees
            BigDecimal totalAmount = order.getQuantity().multiply(slippageAdjustedPrice);
            BigDecimal fees = calculateFees(order, totalAmount);
            
            // Update order
            order.setStatus(OrderStatus.FILLED);
            order.setFilledQuantity(order.getQuantity());
            order.setAvgFillPrice(slippageAdjustedPrice);
            order.setTotalFees(fees);
            order.setExecutedAt(LocalDateTime.now());
            
            orderRepository.save(order);
            
            // Create trade record
            Trade trade = new Trade();
            trade.setOrder(order);
            trade.setUser(order.getUser());
            trade.setPortfolio(order.getPortfolio());
            trade.setInstrumentId(order.getInstrumentId());
            trade.setInstrumentType(order.getInstrumentType());
            trade.setSymbol(order.getSymbol());
            trade.setSide(order.getSide());
            trade.setQuantity(order.getQuantity());
            trade.setPrice(slippageAdjustedPrice);
            trade.setTotalAmount(totalAmount);
            trade.setFees(fees);
            trade.setExpectedPrice(executionPrice); // Price before slippage
            trade.setSlippage(slippage);
            trade.setStatus(TradeStatus.EXECUTED);
            trade.setExecutedAt(LocalDateTime.now());
            
            tradeRepository.save(trade);
            
            // Update position
            updatePosition(trade);
            
            // Update portfolio cash balance
            updatePortfolioCash(order.getPortfolio(), trade);
            
            logger.info("Order {} executed successfully at price {} (slippage: {})", 
                       order.getId(), slippageAdjustedPrice, slippage);
            
        } catch (Exception e) {
            logger.error("Failed to execute order {}", order.getId(), e);
            order.setStatus(OrderStatus.REJECTED);
            order.setNotes("Execution failed: " + e.getMessage());
            orderRepository.save(order);
        }
    }
    
    private BigDecimal calculateSlippage(Order order, MarketDataResponse marketData) {
        // Base slippage depends on asset type and market conditions
        BigDecimal baseSlippage;
        
        String symbol = order.getSymbol().toUpperCase();
        if (symbol.contains("BTC") || symbol.contains("ETH") || symbol.contains("USD")) {
            baseSlippage = BigDecimal.valueOf(0.002); // 0.2% for crypto
        } else if (Arrays.asList("AAPL", "MSFT", "GOOGL", "TSLA", "SPY", "QQQ").contains(symbol)) {
            baseSlippage = BigDecimal.valueOf(0.0005); // 0.05% for liquid stocks
        } else {
            baseSlippage = BigDecimal.valueOf(0.001); // 0.1% for regular stocks
        }
        
        // Adjust for order size (larger orders have more slippage)
        BigDecimal orderValue = order.getQuantity().multiply(marketData.getPrice());
        if (orderValue.compareTo(BigDecimal.valueOf(100000)) > 0) {
            baseSlippage = baseSlippage.multiply(BigDecimal.valueOf(1.5)); // 50% more slippage for large orders
        }
        
        // Adjust for market volatility
        if (marketData.getDayChangePercent() != null) {
            BigDecimal volatilityAdjustment = marketData.getDayChangePercent().abs().multiply(BigDecimal.valueOf(0.1));
            baseSlippage = baseSlippage.add(volatilityAdjustment);
        }
        
        // Add random component
        Random random = new Random();
        BigDecimal randomComponent = BigDecimal.valueOf(random.nextGaussian() * 0.0002); // ±0.02% random
        
        return baseSlippage.add(randomComponent).abs();
    }
    
    private BigDecimal calculateFees(Order order, BigDecimal totalAmount) {
        // Fee structure based on asset type and order size
        BigDecimal feeRate;
        
        String symbol = order.getSymbol().toUpperCase();
        if (symbol.contains("BTC") || symbol.contains("ETH") || symbol.contains("USD")) {
            feeRate = BigDecimal.valueOf(0.0025); // 0.25% for crypto
        } else if (Arrays.asList("SPY", "QQQ", "VTI").contains(symbol)) {
            feeRate = BigDecimal.valueOf(0.0001); // 0.01% for ETFs
        } else {
            feeRate = BigDecimal.valueOf(0.0005); // 0.05% for stocks
        }
        
        // Volume discounts for large orders
        if (totalAmount.compareTo(BigDecimal.valueOf(100000)) > 0) {
            feeRate = feeRate.multiply(BigDecimal.valueOf(0.8)); // 20% discount
        }
        if (totalAmount.compareTo(BigDecimal.valueOf(1000000)) > 0) {
            feeRate = feeRate.multiply(BigDecimal.valueOf(0.7)); // Additional 30% discount
        }
        
        // Minimum fee
        BigDecimal calculatedFee = totalAmount.multiply(feeRate);
        BigDecimal minimumFee = BigDecimal.valueOf(1.0); // $1 minimum
        
        return calculatedFee.max(minimumFee);
    }
    
    private void updatePosition(Trade trade) {
        Optional<Position> existingPosition = positionRepository.findByPortfolioAndSymbolAndInstrumentType(
            trade.getPortfolio(), trade.getSymbol(), trade.getInstrumentType());
        
        Position position;
        if (existingPosition.isPresent()) {
            position = existingPosition.get();
        } else {
            position = new Position();
            position.setPortfolio(trade.getPortfolio());
            position.setInstrumentId(trade.getInstrumentId());
            position.setInstrumentType(trade.getInstrumentType());
            position.setSymbol(trade.getSymbol());
            position.setFirstTradeDate(trade.getTradeDate());
            position.setNetQuantity(BigDecimal.ZERO);
            position.setCostBasis(BigDecimal.ZERO);
            position.setRealizedPnl(BigDecimal.ZERO);
        }
        
        // Calculate new position after trade
        BigDecimal tradeQuantity = trade.getSide() == OrderSide.BUY ? trade.getQuantity() : trade.getQuantity().negate();
        BigDecimal oldQuantity = position.getNetQuantity();
        BigDecimal newQuantity = position.getNetQuantity().add(tradeQuantity);
        
        // Handle position updates based on trade direction
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // Position completely closed - realize P&L
            if (position.getUnrealizedPnl() != null) {
                position.setRealizedPnl(position.getRealizedPnl().add(position.getUnrealizedPnl()));
            }
            position.setNetQuantity(BigDecimal.ZERO);
            position.setAvgPrice(BigDecimal.ZERO);
            position.setCostBasis(BigDecimal.ZERO);
            position.setMarketValue(BigDecimal.ZERO);
            position.setUnrealizedPnl(BigDecimal.ZERO);
            
        } else if (oldQuantity.signum() != newQuantity.signum() && oldQuantity.compareTo(BigDecimal.ZERO) != 0) {
            // Position flipped direction - realize P&L on closed portion
            BigDecimal closedQuantity = oldQuantity.abs();
            BigDecimal realizedPnl = closedQuantity.multiply(trade.getPrice().subtract(position.getAvgPrice()));
            position.setRealizedPnl(position.getRealizedPnl().add(realizedPnl));
            
            // Reset position with remaining quantity
            position.setNetQuantity(newQuantity);
            position.setAvgPrice(trade.getPrice());
            position.setCostBasis(newQuantity.abs().multiply(trade.getPrice()));
            
        } else if (oldQuantity.signum() == tradeQuantity.signum() || oldQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // Adding to existing position or opening new position
            BigDecimal oldCostBasis = position.getCostBasis() != null ? position.getCostBasis() : BigDecimal.ZERO;
            BigDecimal tradeCostBasis = trade.getQuantity().multiply(trade.getPrice());
            BigDecimal newCostBasis = oldCostBasis.add(tradeCostBasis);
            
            BigDecimal newAvgPrice = newCostBasis.divide(newQuantity.abs(), 6, RoundingMode.HALF_UP);
            
            position.setNetQuantity(newQuantity);
            position.setAvgPrice(newAvgPrice);
            position.setCostBasis(newCostBasis);
            
        } else {
            // Reducing existing position - realize P&L on sold portion
            BigDecimal soldQuantity = trade.getQuantity();
            BigDecimal realizedPnl = soldQuantity.multiply(trade.getPrice().subtract(position.getAvgPrice()));
            
            if (trade.getSide() == OrderSide.SELL) {
                position.setRealizedPnl(position.getRealizedPnl().add(realizedPnl));
            } else {
                position.setRealizedPnl(position.getRealizedPnl().subtract(realizedPnl));
            }
            
            position.setNetQuantity(newQuantity);
            // Keep same average price for partial closes
        }
        
        position.setLastTradeDate(trade.getTradeDate());
        position.setTotalTrades(position.getTotalTrades() + 1);
        
        // Update market value and P&L
        updatePositionMarketValue(position);
        
        positionRepository.save(position);
    }
    
    private void updatePositionMarketValue(Position position) {
        if (position.getNetQuantity().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        
        try {
            MarketDataResponse marketData = marketDataService.getLivePrice(position.getSymbol());
            BigDecimal currentPrice = marketData.getPrice();
            
            if (currentPrice == null) {
                logger.warn("No current price available for position: {}", position.getSymbol());
                return;
            }
            
            position.setMarketValue(position.getNetQuantity().multiply(currentPrice));
            position.setUnrealizedPnl(position.getMarketValue().subtract(position.getCostBasis()));
            
        } catch (Exception e) {
            logger.error("Failed to update market value for position {}", position.getId(), e);
        }
    }
    
    private void updatePortfolioCash(Portfolio portfolio, Trade trade) {
        BigDecimal cashImpact;
        
        if (trade.getSide() == OrderSide.BUY) {
            // Reduce cash for buy orders
            cashImpact = trade.getTotalAmount().add(trade.getFees()).negate();
        } else {
            // Increase cash for sell orders
            cashImpact = trade.getTotalAmount().subtract(trade.getFees());
        }
        
        portfolio.setCashBalance(portfolio.getCashBalance().add(cashImpact));
        portfolioRepository.save(portfolio);
    }
    
    private MarketQuoteResponse convertMarketDataToQuote(MarketDataResponse marketData) {
        MarketQuoteResponse quote = new MarketQuoteResponse();
        quote.setSymbol(marketData.getSymbol());
        quote.setInstrumentType(InstrumentType.ASSET);
        quote.setLastPrice(marketData.getPrice());
        quote.setBidPrice(marketData.getBidPrice());
        quote.setAskPrice(marketData.getAskPrice());
        quote.setVolume(marketData.getVolume());
        quote.setDayChange(marketData.getDayChange());
        quote.setDayChangePercent(marketData.getDayChangePercent());
        quote.setQuoteTime(marketData.getDataTimestamp());
        quote.setUpdatedAt(marketData.getDataTimestamp());
        
        // Calculate spread
        if (marketData.getBidPrice() != null && marketData.getAskPrice() != null) {
            BigDecimal spread = marketData.getAskPrice().subtract(marketData.getBidPrice());
            quote.setSpread(spread);
        }
        
        // Determine trend
        if (marketData.getDayChangePercent() != null) {
            if (marketData.getDayChangePercent().compareTo(BigDecimal.ZERO) > 0) {
                quote.setTrend("UP");
            } else if (marketData.getDayChangePercent().compareTo(BigDecimal.ZERO) < 0) {
                quote.setTrend("DOWN");
            } else {
                quote.setTrend("FLAT");
            }
        }
        
        return quote;
    }
    
    private MarketQuoteResponse generateMockQuoteResponse(String symbol) {
        BigDecimal basePrice = getBasePrice(symbol);
        Random random = new Random();
        BigDecimal change = BigDecimal.valueOf(random.nextGaussian() * 0.02); // 2% volatility
        BigDecimal lastPrice = basePrice.multiply(BigDecimal.ONE.add(change));
        
        MarketQuoteResponse quote = new MarketQuoteResponse();
        quote.setSymbol(symbol);
        quote.setInstrumentType(InstrumentType.ASSET);
        quote.setLastPrice(lastPrice);
        quote.setBidPrice(lastPrice.multiply(BigDecimal.valueOf(0.9995))); // 0.05% spread
        quote.setAskPrice(lastPrice.multiply(BigDecimal.valueOf(1.0005)));
        quote.setVolume(BigDecimal.valueOf(100000 + random.nextInt(900000)));
        quote.setDayChange(change.multiply(basePrice));
        quote.setDayChangePercent(change);
        quote.setQuoteTime(LocalDateTime.now());
        quote.setUpdatedAt(LocalDateTime.now());
        quote.setTrend(change.compareTo(BigDecimal.ZERO) >= 0 ? "UP" : "DOWN");
        
        return quote;
    }
    
    private BigDecimal getBasePrice(String symbol) {
        switch (symbol.toUpperCase()) {
            case "AAPL": return BigDecimal.valueOf(169.51);
            case "MSFT": return BigDecimal.valueOf(314.26);
            case "GOOGL": return BigDecimal.valueOf(2485.62);
            case "TSLA": return BigDecimal.valueOf(198.77);
            case "BTCUSD": return BigDecimal.valueOf(45000.00);
            case "ETHUSD": return BigDecimal.valueOf(3000.00);
            default: return BigDecimal.valueOf(100.00);
        }
    }
    
    private boolean canTrade(UserRole role) {
        return role == UserRole.CLIENT || role == UserRole.PORTFOLIO_MANAGER || role == UserRole.ADMIN;
    }
    
    private OrderResponse convertOrderToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setPortfolioId(order.getPortfolio().getId());
        response.setPortfolioName(order.getPortfolio().getName());
        response.setInstrumentId(order.getInstrumentId());
        response.setInstrumentType(order.getInstrumentType());
        response.setSymbol(order.getSymbol());
        response.setSide(order.getSide());
        response.setOrderType(order.getOrderType());
        response.setQuantity(order.getQuantity());
        response.setLimitPrice(order.getLimitPrice());
        response.setStopPrice(order.getStopPrice());
        response.setFilledQuantity(order.getFilledQuantity());
        response.setAvgFillPrice(order.getAvgFillPrice());
        response.setTotalFees(order.getTotalFees());
        response.setStatus(order.getStatus());
        response.setTimeInForce(order.getTimeInForce());
        response.setCreatedAt(order.getCreatedAt());
        response.setSubmittedAt(order.getSubmittedAt());
        response.setExecutedAt(order.getExecutedAt());
        response.setCancelledAt(order.getCancelledAt());
        response.setExpiresAt(order.getExpiresAt());
        response.setNotes(order.getNotes());
        response.setClientOrderId(order.getClientOrderId());
        response.setUserName(order.getUser().getFullName());
        
        // Calculate remaining quantity
        BigDecimal remaining = order.getQuantity().subtract(order.getFilledQuantity());
        response.setRemainingQuantity(remaining);
        
        // Calculate fill percentage
        if (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fillPct = order.getFilledQuantity().divide(order.getQuantity(), 4, RoundingMode.HALF_UP);
            response.setFillPercentage(fillPct);
        }
        
        return response;
    }
    
    private TradeResponse convertTradeToResponse(Trade trade) {
        TradeResponse response = new TradeResponse();
        response.setId(trade.getId());
        response.setOrderId(trade.getOrder().getId());
        response.setPortfolioId(trade.getPortfolio().getId());
        response.setPortfolioName(trade.getPortfolio().getName());
        response.setInstrumentId(trade.getInstrumentId());
        response.setInstrumentType(trade.getInstrumentType());
        response.setSymbol(trade.getSymbol());
        response.setSide(trade.getSide());
        response.setQuantity(trade.getQuantity());
        response.setPrice(trade.getPrice());
        response.setTotalAmount(trade.getTotalAmount());
        response.setFees(trade.getFees());
        response.setExpectedPrice(trade.getExpectedPrice());
        response.setSlippage(trade.getSlippage());
        response.setExecutionVenue(trade.getExecutionVenue());
        response.setStatus(trade.getStatus());
        response.setTradeDate(trade.getTradeDate());
        response.setSettlementDate(trade.getSettlementDate());
        response.setCreatedAt(trade.getCreatedAt());
        response.setExecutedAt(trade.getExecutedAt());
        response.setSettledAt(trade.getSettledAt());
        response.setStrategyId(trade.getStrategyId());
        response.setProductId(trade.getProductId());
        response.setNotes(trade.getNotes());
        response.setExecutionId(trade.getExecutionId());
        response.setUserName(trade.getUser().getFullName());
        
        // Calculate net amount
        BigDecimal netAmount = trade.getSide() == OrderSide.BUY ? 
            trade.getTotalAmount().add(trade.getFees()).negate() : 
            trade.getTotalAmount().subtract(trade.getFees());
        response.setNetAmount(netAmount);
        
        // Determine execution quality
        if (trade.getSlippage() != null) {
            BigDecimal slippagePct = trade.getSlippage().abs();
            if (slippagePct.compareTo(BigDecimal.valueOf(0.001)) <= 0) {
                response.setExecutionQuality("Excellent");
            } else if (slippagePct.compareTo(BigDecimal.valueOf(0.005)) <= 0) {
                response.setExecutionQuality("Good");
            } else if (slippagePct.compareTo(BigDecimal.valueOf(0.01)) <= 0) {
                response.setExecutionQuality("Fair");
            } else {
                response.setExecutionQuality("Poor");
            }
        }
        
        return response;
    }
    
    private PositionResponse convertPositionToResponse(Position position) {
        PositionResponse response = new PositionResponse();
        response.setId(position.getId());
        response.setPortfolioId(position.getPortfolio().getId());
        response.setPortfolioName(position.getPortfolio().getName());
        response.setInstrumentId(position.getInstrumentId());
        response.setInstrumentType(position.getInstrumentType());
        response.setSymbol(position.getSymbol());
        response.setNetQuantity(position.getNetQuantity());
        response.setAvgPrice(position.getAvgPrice());
        response.setCostBasis(position.getCostBasis());
        response.setMarketValue(position.getMarketValue());
        response.setUnrealizedPnl(position.getUnrealizedPnl());
        response.setRealizedPnl(position.getRealizedPnl());
        response.setDelta(position.getDelta());
        response.setGamma(position.getGamma());
        response.setTheta(position.getTheta());
        response.setVega(position.getVega());
        response.setFirstTradeDate(position.getFirstTradeDate());
        response.setLastTradeDate(position.getLastTradeDate());
        response.setTotalTrades(position.getTotalTrades());
        response.setCreatedAt(position.getCreatedAt());
        response.setUpdatedAt(position.getUpdatedAt());
        
        // Get current market data
        try {
            MarketDataResponse marketData = marketDataService.getLivePrice(position.getSymbol());
            response.setCurrentPrice(marketData.getPrice());
            response.setDayChange(marketData.getDayChange());
            response.setDayChangePercent(marketData.getDayChangePercent());
        } catch (Exception e) {
            logger.error("Failed to get market data for position {}", position.getId(), e);
        }
        
        // Calculate position type
        if (position.getNetQuantity().compareTo(BigDecimal.ZERO) > 0) {
            response.setPositionType("Long");
        } else if (position.getNetQuantity().compareTo(BigDecimal.ZERO) < 0) {
            response.setPositionType("Short");
        } else {
            response.setPositionType("Flat");
        }
        
        // Calculate return percentage
        if (position.getCostBasis() != null && position.getCostBasis().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal returnPct = position.getUnrealizedPnl().divide(position.getCostBasis(), 6, RoundingMode.HALF_UP);
            response.setReturnPercent(returnPct);
        }
        
        return response;
    }
    
    private BigDecimal getBasePrice(String symbol) {
        switch (symbol.toUpperCase()) {
            case "AAPL": return BigDecimal.valueOf(172.50);
            case "MSFT": return BigDecimal.valueOf(415.75);
            case "GOOGL": return BigDecimal.valueOf(175.85);
            case "TSLA": return BigDecimal.valueOf(248.50);
            case "AMZN": return BigDecimal.valueOf(185.90);
            case "NVDA": return BigDecimal.valueOf(875.25);
            case "META": return BigDecimal.valueOf(485.60);
            case "NFLX": return BigDecimal.valueOf(485.30);
            case "BTCUSD": return BigDecimal.valueOf(97250.00);
            case "ETHUSD": return BigDecimal.valueOf(3420.50);
            case "SPY": return BigDecimal.valueOf(483.61);
            case "QQQ": return BigDecimal.valueOf(425.80);
            default: return BigDecimal.valueOf(100.00);
        }
    }
}