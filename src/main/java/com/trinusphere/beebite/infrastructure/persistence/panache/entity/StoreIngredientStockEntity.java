package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "store_ingredient_stock")
public class StoreIngredientStockEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "ingredient_id", nullable = false)
    public UUID ingredientId;

    @Column(name = "stock", precision = 12, scale = 3, nullable = false)
    public BigDecimal stock = BigDecimal.ZERO;

    @Column(name = "min_stock", precision = 12, scale = 3, nullable = false)
    public BigDecimal minStock = BigDecimal.ZERO;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}

