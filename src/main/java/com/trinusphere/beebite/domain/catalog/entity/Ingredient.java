package com.trinusphere.beebite.domain.catalog.entity;

import com.trinusphere.beebite.domain.catalog.valueobject.Unit;
import com.trinusphere.beebite.domain.shared.CatalogGuard;
import com.trinusphere.beebite.domain.shared.StoreGuard;

import java.math.BigDecimal;
import java.util.UUID;

public class Ingredient implements StoreGuard.StoreScoped, CatalogGuard.CatalogScoped {
    private UUID id;
    private UUID catalogId;
    private final UUID storeId;
    private String name;
    private BigDecimal stock;
    private Unit unit;
    private BigDecimal costPrice;
    private BigDecimal minStock;
    private boolean active;

    public Ingredient(UUID storeId, String name, BigDecimal stock, Unit unit, BigDecimal costPrice,
            BigDecimal minStock) {
        this(storeId, storeId, name, stock, unit, costPrice, minStock);
    }

    private Ingredient(UUID catalogId, UUID storeId, String name, BigDecimal stock, Unit unit, BigDecimal costPrice,
            BigDecimal minStock) {
        if (storeId == null && catalogId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.catalogId = catalogId != null ? catalogId : storeId;
        this.storeId = storeId != null ? storeId : catalogId;
        this.name = name;
        this.stock = nz(stock);
        this.unit = unit != null ? unit : Unit.UN;
        this.costPrice = nz(costPrice);
        this.minStock = nz(minStock);
        this.active = true;
    }

    public static Ingredient reconstitute(UUID id, UUID storeId, String name, BigDecimal stock, Unit unit,
            BigDecimal costPrice, BigDecimal minStock, boolean active) {
        return reconstituteForCatalog(id, storeId, storeId, name, stock, unit, costPrice, minStock, active);
    }

    public static Ingredient reconstituteForCatalog(UUID id, UUID catalogId, UUID contextStoreId, String name,
            BigDecimal stock, Unit unit, BigDecimal costPrice, BigDecimal minStock, boolean active) {
        Ingredient i = new Ingredient(catalogId, contextStoreId, name, stock, unit, costPrice, minStock);
        i.id = id;
        i.active = active;
        return i;
    }

    public void assignCatalog(UUID catalogId) {
        if (catalogId != null) {
            this.catalogId = catalogId;
        }
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

    @Override
    public UUID getCatalogId() {
        return catalogId;
    }

    @Override
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
