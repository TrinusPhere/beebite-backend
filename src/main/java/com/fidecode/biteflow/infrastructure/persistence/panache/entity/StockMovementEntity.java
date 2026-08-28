package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movement")
public class StockMovementEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @Column(name = "store_id", nullable = false)
    public UUID storeId;
    @Column(name = "ingredient_id", nullable = false)
    public UUID ingredientId;
    @Column(nullable = false)
    public String type;
    @Column(nullable = false, precision = 12, scale = 3)
    public BigDecimal quantity;
    @Column(name = "previous_stock", nullable = false, precision = 12, scale = 3)
    public BigDecimal previousStock;
    @Column(name = "resulting_stock", nullable = false, precision = 12, scale = 3)
    public BigDecimal resultingStock;
    @Column(nullable = false)
    public String reason;
    @Column(name = "document_ref")
    public String document;
    @Column(name = "source_type")
    public String sourceType;
    @Column(name = "source_id")
    public UUID sourceId;
    @Column(name = "created_by")
    public UUID createdBy;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
}
