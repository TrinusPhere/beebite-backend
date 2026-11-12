package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "order_item")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    public OrderEntity order;

    @Column(name = "product_id")
    public UUID productId;

    @Column(name = "product_name", nullable = false)
    public String productName;

    @Column(name = "quantity", nullable = false)
    public int quantity;

    @Column(name = "unit_price", nullable = false)
    public BigDecimal unitPrice;

    @Column(name = "extra_price", nullable = false)
    public BigDecimal extraPrice;

    @Column(name = "total_price", nullable = false)
    public BigDecimal totalPrice;

    @Column(name = "observation")
    public String observation;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OrderItemModifierEntity> modifiers = new ArrayList<>();

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OrderItemRemovalEntity> removals = new ArrayList<>();
}
