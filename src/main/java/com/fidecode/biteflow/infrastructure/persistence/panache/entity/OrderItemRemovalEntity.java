package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "order_item_removal")
public class OrderItemRemovalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    public OrderItemEntity orderItem;

    @Column(name = "ingredient_id")
    public UUID ingredientId;

    @Column(name = "ingredient_name", nullable = false)
    public String ingredientName;
}
