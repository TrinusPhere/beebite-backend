package com.trinusphere.beebite.domain.inventory.entity;

import com.trinusphere.beebite.domain.inventory.valueobject.StockMovementType;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class StockMovement implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private final UUID ingredientId;
    private final StockMovementType type;
    private final BigDecimal quantity;
    private final BigDecimal previousStock;
    private final BigDecimal resultingStock;
    private final String reason;
    private final String document;
    private String sourceType;
    private UUID sourceId;
    private UUID createdBy;
    private LocalDateTime createdAt;

    public StockMovement(UUID storeId, UUID ingredientId, StockMovementType type, BigDecimal quantity,
            BigDecimal previousStock, BigDecimal resultingStock, String reason) {
        if (storeId == null || ingredientId == null || type == null)
            throw new IllegalArgumentException("Dados da movimentação são obrigatórios.");
        if (quantity == null || quantity.signum() <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("Motivo é obrigatório.");
        this.storeId = storeId;
        this.ingredientId = ingredientId;
        this.type = type;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.resultingStock = resultingStock;
        this.reason = reason;
        this.document = null;
        this.createdAt = LocalDateTime.now();
    }

    private StockMovement(UUID storeId, UUID ingredientId, StockMovementType type, BigDecimal quantity,
            BigDecimal previousStock, BigDecimal resultingStock, String reason, String document) {
        this.storeId = storeId;
        this.ingredientId = ingredientId;
        this.type = type;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.resultingStock = resultingStock;
        this.reason = reason;
        this.document = document;
        this.createdAt = LocalDateTime.now();
    }

    public static StockMovement create(UUID storeId, UUID ingredientId, StockMovementType type, BigDecimal quantity,
            BigDecimal previousStock, BigDecimal resultingStock, String reason, String document) {
        return new StockMovement(storeId, ingredientId, type, quantity, previousStock, resultingStock, reason,
                document);
    }

    public static StockMovement reconstitute(UUID id, UUID storeId, UUID ingredientId, StockMovementType type,
            BigDecimal quantity, BigDecimal previousStock, BigDecimal resultingStock, String reason, String document,
            String sourceType, UUID sourceId, UUID createdBy, LocalDateTime createdAt) {
        var m = new StockMovement(storeId, ingredientId, type, quantity, previousStock, resultingStock, reason,
                document);
        m.id = id;
        m.sourceType = sourceType;
        m.sourceId = sourceId;
        m.createdBy = createdBy;
        m.createdAt = createdAt;
        return m;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public UUID getIngredientId() {
        return ingredientId;
    }

    public StockMovementType getType() {
        return type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPreviousStock() {
        return previousStock;
    }

    public BigDecimal getResultingStock() {
        return resultingStock;
    }

    public String getReason() {
        return reason;
    }

    public String getDocument() {
        return document;
    }

    public String getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void linkSource(String type, UUID id) {
        this.sourceType = type;
        this.sourceId = id;
    }

    public void recordCreatedBy(UUID id) {
        this.createdBy = id;
    }
}
