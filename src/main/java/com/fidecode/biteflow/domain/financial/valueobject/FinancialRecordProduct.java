package com.fidecode.biteflow.domain.financial.valueobject;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Value Object — snapshot de um produto vendido dentro de um registro financeiro. POJO puro, imutável.
 */
public class FinancialRecordProduct {

    private UUID id;
    private final UUID productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal price;

    public FinancialRecordProduct(UUID productId, String productName, int quantity, BigDecimal price) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName é obrigatório.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity deve ser maior que zero.");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    // ── Getters ──
    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    // ── Setter para persistência ──
    public void setId(UUID id) {
        this.id = id;
    }
}
