package com.fidecode.biteflow.domain.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Item de um pedido — snapshot de preço do produto no momento da criação.
 * totalPrice = (unitPrice + extraPrice) × quantity
 */
public class OrderItem {

    private UUID id;
    private UUID productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal extraPrice;
    private BigDecimal totalPrice;
    private String observation;
    private List<OrderItemModifier> modifiers;
    private List<OrderItemRemoval> removals;

    public OrderItem(UUID productId, String productName, int quantity,
                     BigDecimal unitPrice, String observation) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName é obrigatório.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity deve ser maior que zero.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("unitPrice não pode ser negativo.");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.extraPrice = BigDecimal.ZERO;
        this.observation = observation;
        this.modifiers = new ArrayList<>();
        this.removals = new ArrayList<>();
        recalculateTotalPrice();
    }

    /**
     * Recalcula totalPrice = (unitPrice + extraPrice) × quantity.
     */
    public void recalculateTotalPrice() {
        BigDecimal extra = modifiers != null
            ? modifiers.stream().map(OrderItemModifier::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
            : BigDecimal.ZERO;
        this.extraPrice = extra;
        this.totalPrice = unitPrice.add(extraPrice).multiply(BigDecimal.valueOf(quantity));
    }

    public void addModifier(OrderItemModifier modifier) {
        this.modifiers.add(modifier);
        recalculateTotalPrice();
    }

    public void addRemoval(OrderItemRemoval removal) {
        this.removals.add(removal);
    }

    // ── Getters ──
    public UUID getId()                         { return id; }
    public UUID getProductId()                  { return productId; }
    public String getProductName()              { return productName; }
    public int getQuantity()                    { return quantity; }
    public BigDecimal getUnitPrice()            { return unitPrice; }
    public BigDecimal getExtraPrice()           { return extraPrice; }
    public BigDecimal getTotalPrice()           { return totalPrice; }
    public String getObservation()              { return observation; }
    public List<OrderItemModifier> getModifiers() { return modifiers; }
    public List<OrderItemRemoval> getRemovals()   { return removals; }

    // ── Setters ──
    public void setId(UUID id)                                   { this.id = id; }
    public void setModifiers(List<OrderItemModifier> modifiers)  { this.modifiers = modifiers != null ? modifiers : new ArrayList<>(); }
    public void setRemovals(List<OrderItemRemoval> removals)     { this.removals = removals != null ? removals : new ArrayList<>(); }
    public void setExtraPrice(BigDecimal extraPrice)             { this.extraPrice = extraPrice != null ? extraPrice : BigDecimal.ZERO; }
    public void setTotalPrice(BigDecimal totalPrice)             { this.totalPrice = totalPrice; }
}
