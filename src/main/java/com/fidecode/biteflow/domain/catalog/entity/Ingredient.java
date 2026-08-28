package com.fidecode.biteflow.domain.catalog.entity;

import com.fidecode.biteflow.domain.catalog.valueobject.Unit;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import java.math.BigDecimal;
import java.util.UUID;

public class Ingredient implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private String name;
    private BigDecimal stock;
    private Unit unit;
    private BigDecimal costPrice;
    private BigDecimal minStock;
    private boolean active;

    public Ingredient(UUID storeId, String name, BigDecimal stock, Unit unit, BigDecimal costPrice,
            BigDecimal minStock) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.stock = nz(stock);
        this.unit = unit != null ? unit : Unit.UN;
        this.costPrice = nz(costPrice);
        this.minStock = nz(minStock);
        this.active = true;
    }

    public static Ingredient reconstitute(UUID id, UUID storeId, String name, BigDecimal stock, Unit unit,
            BigDecimal costPrice, BigDecimal minStock, boolean active) {
        Ingredient i = new Ingredient(storeId, name, stock, unit, costPrice, minStock);
        i.id = id;
        i.active = active;
        return i;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static void positive(BigDecimal q) {
        if (q == null || q.signum() <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
    }

    public void updateDetails(String name, Unit unit, BigDecimal costPrice, BigDecimal minStock, Boolean active) {
        if (name != null && !name.isBlank())
            this.name = name;
        if (unit != null)
            this.unit = unit;
        if (costPrice != null)
            this.costPrice = costPrice;
        if (minStock != null)
            this.minStock = minStock;
        if (active != null)
            this.active = active;
    }

    public boolean isLowStock() {
        return stock.compareTo(minStock) < 0;
    }

    public void increaseStock(BigDecimal q) {
        positive(q);
        stock = stock.add(q);
    }

    public void decreaseStock(BigDecimal q) {
        positive(q);
        var n = stock.subtract(q);
        if (n.signum() < 0)
            throw new IllegalArgumentException("Estoque não pode ficar negativo.");
        stock = n;
    }

    public void adjustStock(BigDecimal n) {
        if (n == null || n.signum() < 0)
            throw new IllegalArgumentException("Saldo resultante inválido.");
        stock = n;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public Unit getUnit() {
        return unit;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getMinStock() {
        return minStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}