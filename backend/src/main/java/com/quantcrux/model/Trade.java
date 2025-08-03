package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @Column(name = "instrument_id")
    private UUID instrumentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false)
    private InstrumentType instrumentType;
    
    @NotNull
    @Column(nullable = false, length = 50)
    private String symbol;
    
    // Trade details
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;
    
    @NotNull
    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal quantity;
    
    @NotNull
    @Column(precision = 15, scale = 6, nullable = false)
    private BigDecimal price;
    
    @NotNull
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;
    
    // Execution quality
    @Column(name = "expected_price", precision = 15, scale = 6)
    private BigDecimal expectedPrice;
    
    @Column(precision = 15, scale = 6)
    private BigDecimal slippage;
    
    @Column(name = "execution_venue", length = 50)
    private String executionVenue = "INTERNAL";
    
    // Status and dates
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status = TradeStatus.EXECUTED;
    
    @NotNull
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate = LocalDate.now();
    
    @Column(name = "settlement_date")
    private LocalDate settlementDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "executed_at")
    private LocalDateTime executedAt = LocalDateTime.now();
    
    @Column(name = "settled_at")
    private LocalDateTime settledAt;
    
    // References
    @Column(name = "strategy_id")
    private UUID strategyId;
    
    @Column(name = "product_id")
    private UUID productId;
    
    // Metadata
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "execution_id", length = 100)
    private String executionId;
    
    // Constructors
    public Trade() {}
    
    public Trade(Order order, User user, Portfolio portfolio, InstrumentType instrumentType, 
                String symbol, OrderSide side, BigDecimal quantity, BigDecimal price) {
        this.order = order;
        this.user = user;
        this.portfolio = portfolio;
        this.instrumentType = instrumentType;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = quantity.multiply(price);
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    
    public UUID getInstrumentId() { return instrumentId; }
    public void setInstrumentId(UUID instrumentId) { this.instrumentId = instrumentId; }
    
    public InstrumentType getInstrumentType() { return instrumentType; }
    public void setInstrumentType(InstrumentType instrumentType) { this.instrumentType = instrumentType; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    
    public BigDecimal getExpectedPrice() { return expectedPrice; }
    public void setExpectedPrice(BigDecimal expectedPrice) { this.expectedPrice = expectedPrice; }
    
    public BigDecimal getSlippage() { return slippage; }
    public void setSlippage(BigDecimal slippage) { this.slippage = slippage; }
    
    public String getExecutionVenue() { return executionVenue; }
    public void setExecutionVenue(String executionVenue) { this.executionVenue = executionVenue; }
    
    public TradeStatus getStatus() { return status; }
    public void setStatus(TradeStatus status) { this.status = status; }
    
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    
    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    
    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
    
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
}