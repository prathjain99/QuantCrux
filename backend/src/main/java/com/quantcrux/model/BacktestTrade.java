package com.quantcrux.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_trades")
public class BacktestTrade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backtest_id", nullable = false)
    private Backtest backtest;
    
    @Column(name = "trade_number", nullable = false)
    private Integer tradeNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false)
    private SignalType signalType;
    
    // Entry details
    @NotNull
    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;
    
    @NotNull
    @Column(name = "entry_price", precision = 15, scale = 6, nullable = false)
    private BigDecimal entryPrice;
    
    @Column(name = "entry_reason", columnDefinition = "TEXT")
    private String entryReason;
    
    @Column(name = "entry_indicators", columnDefinition = "TEXT")
    private String entryIndicators;
    
    // Exit details
    @Column(name = "exit_time")
    private LocalDateTime exitTime;
    
    @Column(name = "exit_price", precision = 15, scale = 6)
    private BigDecimal exitPrice;
    
    @Column(name = "exit_reason", columnDefinition = "TEXT")
    private String exitReason;
    
    @Column(name = "exit_indicators", columnDefinition = "TEXT")
    private String exitIndicators;
    
    // Trade results
    @NotNull
    @Column(precision = 15, scale = 6, nullable = false)
    private BigDecimal quantity;
    
    @Column(name = "gross_pnl", precision = 15, scale = 6)
    private BigDecimal grossPnl;
    
    @Column(name = "net_pnl", precision = 15, scale = 6)
    private BigDecimal netPnl;
    
    @Column(name = "return_pct", precision = 10, scale = 6)
    private BigDecimal returnPct;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    // Position info
    @Column(name = "position_size_pct", precision = 8, scale = 4)
    private BigDecimal positionSizePct;
    
    @Column(name = "commission_paid", precision = 15, scale = 6)
    private BigDecimal commissionPaid;
    
    @Column(name = "slippage_cost", precision = 15, scale = 6)
    private BigDecimal slippageCost;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public BacktestTrade() {}
    
    public BacktestTrade(Backtest backtest, Integer tradeNumber, SignalType signalType, 
                        LocalDateTime entryTime, BigDecimal entryPrice, BigDecimal quantity) {
        this.backtest = backtest;
        this.tradeNumber = tradeNumber;
        this.signalType = signalType;
        this.entryTime = entryTime;
        this.entryPrice = entryPrice;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Backtest getBacktest() { return backtest; }
    public void setBacktest(Backtest backtest) { this.backtest = backtest; }
    
    public Integer getTradeNumber() { return tradeNumber; }
    public void setTradeNumber(Integer tradeNumber) { this.tradeNumber = tradeNumber; }
    
    public SignalType getSignalType() { return signalType; }
    public void setSignalType(SignalType signalType) { this.signalType = signalType; }
    
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    
    public BigDecimal getEntryPrice() { return entryPrice; }
    public void setEntryPrice(BigDecimal entryPrice) { this.entryPrice = entryPrice; }
    
    public String getEntryReason() { return entryReason; }
    public void setEntryReason(String entryReason) { this.entryReason = entryReason; }
    
    public String getEntryIndicators() { return entryIndicators; }
    public void setEntryIndicators(String entryIndicators) { this.entryIndicators = entryIndicators; }
    
    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    
    public BigDecimal getExitPrice() { return exitPrice; }
    public void setExitPrice(BigDecimal exitPrice) { this.exitPrice = exitPrice; }
    
    public String getExitReason() { return exitReason; }
    public void setExitReason(String exitReason) { this.exitReason = exitReason; }
    
    public String getExitIndicators() { return exitIndicators; }
    public void setExitIndicators(String exitIndicators) { this.exitIndicators = exitIndicators; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getGrossPnl() { return grossPnl; }
    public void setGrossPnl(BigDecimal grossPnl) { this.grossPnl = grossPnl; }
    
    public BigDecimal getNetPnl() { return netPnl; }
    public void setNetPnl(BigDecimal netPnl) { this.netPnl = netPnl; }
    
    public BigDecimal getReturnPct() { return returnPct; }
    public void setReturnPct(BigDecimal returnPct) { this.returnPct = returnPct; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public BigDecimal getPositionSizePct() { return positionSizePct; }
    public void setPositionSizePct(BigDecimal positionSizePct) { this.positionSizePct = positionSizePct; }
    
    public BigDecimal getCommissionPaid() { return commissionPaid; }
    public void setCommissionPaid(BigDecimal commissionPaid) { this.commissionPaid = commissionPaid; }
    
    public BigDecimal getSlippageCost() { return slippageCost; }
    public void setSlippageCost(BigDecimal slippageCost) { this.slippageCost = slippageCost; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}