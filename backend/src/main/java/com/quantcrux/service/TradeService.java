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
    
    private final Random random = new Random();
    
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
        List<MarketQuote> quotes = new ArrayList<>();
        
        for (String symbol : symbols) {
            Optional<MarketQuote> quote = marketQuoteRepository.findBySymbolAndInstrumentType(symbol, InstrumentType.ASSET);
            if (quote.isPresent()) {
                quotes.add(quote.get());
            } else {
                // Generate mock quote if not found
                MarketQuote mockQuote = generateMockQuote(symbol);
                quotes.add(mockQuote);
            }
        }
        
        return quotes.stream()
                .map(this::convertQuoteToResponse)
                .collect(Collectors.toList());
    }
    
    public MarketQuoteResponse getMarketQuote(String symbol, InstrumentType instrumentType) {
        Optional<MarketQuote> quote = marketQuoteRepository.findBySymbolAndInstrumentType(symbol, instrumentType);
        
        if (quote.isPresent()) {
            return convertQuoteToResponse(quote.get());
        } else {
            // Generate mock quote
            MarketQuote mockQuote = generateMockQuote(symbol);
            return convertQuoteToResponse(mockQuote);
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
            // Get current market price
            MarketQuoteResponse quote = getMarketQuote(request.getSymbol(), request.getInstrumentType());
            price = request.getSide() == OrderSide.BUY ? quote.getAskPrice() : quote.getBidPrice();
            if (price == null) {
                price = quote.getLastPrice();
            }
        } else if (request.getLimitPrice() != null) {
            price = request.getLimitPrice();
        } else {
            // Fallback to last price
            MarketQuoteResponse quote = getMarketQuote(request.getSymbol(), request.getInstrumentType());
            price = quote.getLastPrice();
        }
        
        BigDecimal totalCost = request.getQuantity().multiply(price);
        BigDecimal fees = totalCost.multiply(BigDecimal.valueOf(0.001)); // 0.1% commission
        
        return totalCost.add(fees);
    }
    
    private void executeOrder(Order order) {
        try {
            logger.info("Executing order {}", order.getId());
            
            // Get market price
            MarketQuoteResponse quote = getMarketQuote(order.getSymbol(), order.getInstrumentType());
            BigDecimal executionPrice = order.getSide() == OrderSide.BUY ? quote.getAskPrice() : quote.getBidPrice();
            if (executionPrice == null) {
                executionPrice = quote.getLastPrice();
            }
            
            // Add realistic slippage
            BigDecimal slippage = BigDecimal.valueOf((random.nextGaussian() * 0.001)); // 0.1% average slippage
            executionPrice = executionPrice.multiply(BigDecimal.ONE.add(slippage));
            
            // Calculate fees
            BigDecimal totalAmount = order.getQuantity().multiply(executionPrice);
            BigDecimal fees = totalAmount.multiply(BigDecimal.valueOf(0.001)); // 0.1% commission
            
            // Update order
            order.setStatus(OrderStatus.FILLED);
            order.setFilledQuantity(order.getQuantity());
            order.setAvgFillPrice(executionPrice);
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
            trade.setPrice(executionPrice);
            trade.setTotalAmount(totalAmount);
            trade.setFees(fees);
            trade.setExpectedPrice(quote.getLastPrice());
            trade.setSlippage(slippage);
            trade.setStatus(TradeStatus.EXECUTED);
            trade.setExecutedAt(LocalDateTime.now());
            
            tradeRepository.save(trade);
            
            // Update position
            updatePosition(trade);
            
            // Update portfolio cash balance
            updatePortfolioCash(order.getPortfolio(), trade);
            
            logger.info("Order {} executed successfully at price {}", order.getId(), executionPrice);
            
        } catch (Exception e) {
            logger.error("Failed to execute order {}", order.getId(), e);
            order.setStatus(OrderStatus.REJECTED);
            order.setNotes("Execution failed: " + e.getMessage());
            orderRepository.save(order);
        }
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
        }
        
        // Update position based on trade
        BigDecimal tradeQuantity = trade.getSide() == OrderSide.BUY ? trade.getQuantity() : trade.getQuantity().negate();
        BigDecimal newQuantity = position.getNetQuantity().add(tradeQuantity);
        
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // Position closed
            position.setRealizedPnl(position.getRealizedPnl().add(position.getUnrealizedPnl()));
            position.setNetQuantity(BigDecimal.ZERO);
            position.setAvgPrice(BigDecimal.ZERO);
            position.setCostBasis(BigDecimal.ZERO);
            position.setMarketValue(BigDecimal.ZERO);
            position.setUnrealizedPnl(BigDecimal.ZERO);
        } else {
            // Update average price for same-side trades
            if (position.getNetQuantity().signum() == tradeQuantity.signum() || position.getNetQuantity().compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal totalCost = position.getCostBasis().add(trade.getTotalAmount());
                BigDecimal totalQuantity = position.getNetQuantity().abs().add(trade.getQuantity());
                BigDecimal newAvgPrice = totalCost.divide(totalQuantity, 6, RoundingMode.HALF_UP);
                
                position.setAvgPrice(newAvgPrice);
                position.setCostBasis(totalQuantity.multiply(newAvgPrice));
            }
            
            position.setNetQuantity(newQuantity);
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
            MarketQuoteResponse quote = getMarketQuote(position.getSymbol(), position.getInstrumentType());
            BigDecimal currentPrice = quote.getLastPrice();
            
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
    
    private MarketQuote generateMockQuote(String symbol) {
        BigDecimal basePrice = getBasePrice(symbol);
        BigDecimal change = BigDecimal.valueOf(random.nextGaussian() * 0.02); // 2% volatility
        BigDecimal lastPrice = basePrice.multiply(BigDecimal.ONE.add(change));
        
        MarketQuote quote = new MarketQuote();
        quote.setSymbol(symbol);
        quote.setInstrumentType(InstrumentType.ASSET);
        quote.setLastPrice(lastPrice);
        quote.setBidPrice(lastPrice.multiply(BigDecimal.valueOf(0.9995))); // 0.05% spread
        quote.setAskPrice(lastPrice.multiply(BigDecimal.valueOf(1.0005)));
        quote.setOpenPrice(basePrice);
        quote.setHighPrice(lastPrice.multiply(BigDecimal.valueOf(1.01)));
        quote.setLowPrice(lastPrice.multiply(BigDecimal.valueOf(0.99)));
        quote.setPrevClose(basePrice);
        quote.setVolume(BigDecimal.valueOf(100000 + random.nextInt(900000)));
        
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
            MarketQuoteResponse quote = getMarketQuote(position.getSymbol(), position.getInstrumentType());
            response.setCurrentPrice(quote.getLastPrice());
            response.setDayChange(quote.getDayChange());
            response.setDayChangePercent(quote.getDayChangePercent());
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
    
    private MarketQuoteResponse convertQuoteToResponse(MarketQuote quote) {
        MarketQuoteResponse response = new MarketQuoteResponse();
        response.setSymbol(quote.getSymbol());
        response.setInstrumentType(quote.getInstrumentType());
        response.setBidPrice(quote.getBidPrice());
        response.setAskPrice(quote.getAskPrice());
        response.setLastPrice(quote.getLastPrice());
        response.setVolume(quote.getVolume());
        response.setOpenPrice(quote.getOpenPrice());
        response.setHighPrice(quote.getHighPrice());
        response.setLowPrice(quote.getLowPrice());
        response.setPrevClose(quote.getPrevClose());
        response.setQuoteTime(quote.getQuoteTime());
        response.setUpdatedAt(quote.getUpdatedAt());
        
        // Calculate day change
        if (quote.getPrevClose() != null) {
            BigDecimal dayChange = quote.getLastPrice().subtract(quote.getPrevClose());
            BigDecimal dayChangePct = dayChange.divide(quote.getPrevClose(), 6, RoundingMode.HALF_UP);
            response.setDayChange(dayChange);
            response.setDayChangePercent(dayChangePct);
            
            // Determine trend
            if (dayChange.compareTo(BigDecimal.ZERO) > 0) {
                response.setTrend("UP");
            } else if (dayChange.compareTo(BigDecimal.ZERO) < 0) {
                response.setTrend("DOWN");
            } else {
                response.setTrend("FLAT");
            }
        }
        
        // Calculate spread
        if (quote.getBidPrice() != null && quote.getAskPrice() != null) {
            BigDecimal spread = quote.getAskPrice().subtract(quote.getBidPrice());
            response.setSpread(spread);
        }
        
        return response;
    }
}