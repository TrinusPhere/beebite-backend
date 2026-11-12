package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_combo_component")
public class ProductComboComponentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "combo_product_id", nullable = false)
    public UUID comboProductId;

    @Column(name = "component_product_id", nullable = false)
    public UUID componentProductId;

    @Column(nullable = false)
    public int quantity;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}

