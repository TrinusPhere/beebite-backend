package com.fidecode.biteflow.domain.order.entity;

import com.fidecode.biteflow.domain.order.valueobject.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root — Pedido.
 * POJO puro, sem dependência de framework.
 */
public class Order {

    private UUID id;
    private UUID storeId;
    private String displayId;
    private UUID customerId;
    private String customerName;
    private String customerAddress;
    private OrderType type;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private UUID driverId;
    private String driverName;
    private OrderPlatform platform;
    private String externalOrderId;
    private BigDecimal subtotal;
    private BigDecimal deliveryCost;
    private BigDecimal totalValue;
    private boolean nfeEmitted;
    private List<OrderItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime finishedAt;

    public Order(UUID storeId, OrderType type, PaymentMethod paymentMethod) {
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (type == null) throw new IllegalArgumentException("type é obrigatório.");
        if (paymentMethod == null) throw new IllegalArgumentException("paymentMethod é obrigatório.");
        this.storeId = storeId;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.ANALISE;
        this.platform = OrderPlatform.OWN;
        this.subtotal = BigDecimal.ZERO;
        this.deliveryCost = BigDecimal.ZERO;
        this.totalValue = BigDecimal.ZERO;
        this.nfeEmitted = false;
        this.items = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adiciona um item ao pedido e recalcula totais.
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotals();
    }

    /**
     * Recalcula subtotal e totalValue a partir dos itens.
     * totalValue = subtotal + deliveryCost
     */
    public void recalculateTotals() {
        this.subtotal = items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalValue = subtotal.add(deliveryCost != null ? deliveryCost : BigDecimal.ZERO);
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsNfeEmitted() {
        this.nfeEmitted = true;
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters ──
    public UUID getId()                    { return id; }
    public UUID getStoreId()               { return storeId; }
    public String getDisplayId()           { return displayId; }
    public UUID getCustomerId()            { return customerId; }
    public String getCustomerName()        { return customerName; }
    public String getCustomerAddress()     { return customerAddress; }
    public OrderType getType()             { return type; }
    public OrderStatus getStatus()         { return status; }
    public PaymentMethod getPaymentMethod(){ return paymentMethod; }
    public UUID getDriverId()              { return driverId; }
    public String getDriverName()          { return driverName; }
    public OrderPlatform getPlatform()     { return platform; }
    public String getExternalOrderId()     { return externalOrderId; }
    public BigDecimal getSubtotal()        { return subtotal; }
    public BigDecimal getDeliveryCost()    { return deliveryCost; }
    public BigDecimal getTotalValue()      { return totalValue; }
    public boolean isNfeEmitted()          { return nfeEmitted; }
    public List<OrderItem> getItems()      { return items; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public LocalDateTime getUpdatedAt()    { return updatedAt; }
    public LocalDateTime getFinishedAt()   { return finishedAt; }

    // ── Setters ──
    public void setId(UUID id)                             { this.id = id; }
    public void setDisplayId(String displayId)             { this.displayId = displayId; }
    public void setCustomerId(UUID customerId)             { this.customerId = customerId; }
    public void setCustomerName(String customerName)       { this.customerName = customerName; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public void setType(OrderType type)                    { this.type = type; }
    public void setStatus(OrderStatus status)              { this.status = status; }
    public void setPaymentMethod(PaymentMethod pm)         { this.paymentMethod = pm; }
    public void setDriverId(UUID driverId)                 { this.driverId = driverId; }
    public void setDriverName(String driverName)           { this.driverName = driverName; }
    public void setPlatform(OrderPlatform platform)        { this.platform = platform; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    public void setSubtotal(BigDecimal subtotal)           { this.subtotal = subtotal; }
    public void setDeliveryCost(BigDecimal deliveryCost)   { this.deliveryCost = deliveryCost; }
    public void setTotalValue(BigDecimal totalValue)       { this.totalValue = totalValue; }
    public void setNfeEmitted(boolean nfeEmitted)          { this.nfeEmitted = nfeEmitted; }
    public void setItems(List<OrderItem> items)            { this.items = items != null ? items : new ArrayList<>(); }
    public void setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)      { this.updatedAt = updatedAt; }
    public void setFinishedAt(LocalDateTime finishedAt)    { this.finishedAt = finishedAt; }
}
