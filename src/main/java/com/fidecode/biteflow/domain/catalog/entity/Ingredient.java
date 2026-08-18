package com.fidecode.biteflow.domain.catalog.entity;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import com.fidecode.biteflow.domain.catalog.valueobject.Unit;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain entity — Ingredient.
 * POJO puro, sem dependência de framework.
 */
public class Ingredient implements StoreGuard.StoreScoped {

    private UUID id;
    private UUID storeId;
    private String name;
    private BigDecimal stock;
    private Unit unit;
    private BigDecimal costPrice;
    private BigDecimal minStock;

    public Ingredient(UUID storeId, String name, BigDecimal stock, Unit unit, BigDecimal costPrice, BigDecimal minStock) {
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.stock = stock != null ? stock : BigDecimal.ZERO;
        this.unit = unit != null ? unit : Unit.UN;
        this.costPrice = costPrice != null ? costPrice : BigDecimal.ZERO;
        this.minStock = minStock != null ? minStock : BigDecimal.ZERO;
    }

    /**
     * Retorna true se o estoque está abaixo do mínimo.
     */
    public boolean isLowStock() {
        return stock.compareTo(minStock) < 0;
    }

    // ── Getters ──
    public UUID getId()             { return id; }
    public UUID getStoreId()        { return storeId; }
    public String getName()         { return name; }
    public BigDecimal getStock()    { return stock; }
    public Unit getUnit()           { return unit; }
    public BigDecimal getCostPrice(){ return costPrice; }
    public BigDecimal getMinStock() { return minStock; }

    // ── Setters ──
    public void setId(UUID id)                      { this.id = id; }
    public void setName(String name)                { this.name = name; }
    public void setStock(BigDecimal stock)           { this.stock = stock; }
    public void setUnit(Unit unit)                   { this.unit = unit; }
    public void setCostPrice(BigDecimal costPrice)   { this.costPrice = costPrice; }
    public void setMinStock(BigDecimal minStock)     { this.minStock = minStock; }
}
