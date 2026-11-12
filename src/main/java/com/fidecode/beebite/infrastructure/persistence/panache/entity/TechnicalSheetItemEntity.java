package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "technical_sheet_item")
public class TechnicalSheetItemEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public ProductEntity product;

    @Column(name = "ingredient_id", nullable = false)
    public UUID ingredientId;

    @Column(precision = 10, scale = 3)
    public BigDecimal quantity;

    @Column(name = "cost_snapshot", precision = 10, scale = 2)
    public BigDecimal costSnapshot;
}
