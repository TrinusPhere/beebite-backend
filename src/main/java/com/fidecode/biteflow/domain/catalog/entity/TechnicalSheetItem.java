package com.fidecode.biteflow.domain.catalog.entity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain entity — Item da ficha técnica de um produto.
 */
public class TechnicalSheetItem {

    private UUID id;
    private UUID productId;
    private UUID ingredientId;
    private BigDecimal quantity;
    private BigDecimal costSnapshot;

    public TechnicalSheetItem(UUID productId, UUID ingredientId, BigDecimal quantity, BigDecimal costSnapshot) {
        if (productId == null) throw new IllegalArgumentException("productId é obrigatório.");
        if (ingredientId == null) throw new IllegalArgumentException("ingredientId é obrigatório.");
        this.productId = productId;
        this.ingredientId = ingredientId;
        this.quantity = quantity != null ? quantity : BigDecimal.ONE;
        this.costSnapshot = costSnapshot != null ? costSnapshot : BigDecimal.ZERO;
    }

    /**
     * Custo da linha = quantity × costSnapshot
     */
    public BigDecimal getLineCost() {
        return quantity.multiply(costSnapshot);
    }

    // ── Getters ──
    public UUID getId()                 { return id; }
    public UUID getProductId()          { return productId; }
    public UUID getIngredientId()       { return ingredientId; }
    public BigDecimal getQuantity()     { return quantity; }
    public BigDecimal getCostSnapshot() { return costSnapshot; }

    // ── Setters ──
    public void setId(UUID id)                          { this.id = id; }
    public void setProductId(UUID productId)            { this.productId = productId; }
    public void setQuantity(BigDecimal quantity)         { this.quantity = quantity; }
    public void setCostSnapshot(BigDecimal costSnapshot) { this.costSnapshot = costSnapshot; }
}
