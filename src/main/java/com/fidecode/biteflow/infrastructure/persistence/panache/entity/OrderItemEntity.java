package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "order_id", nullable = false)
    public UUID orderId;

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
}
