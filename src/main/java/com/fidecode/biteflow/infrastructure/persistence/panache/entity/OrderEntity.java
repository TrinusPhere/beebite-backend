package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "display_id", nullable = false)
    public String displayId;

    @Column(name = "customer_id")
    public UUID customerId;

    @Column(name = "customer_name")
    public String customerName;

    @Column(name = "customer_address")
    public String customerAddress;

    @Column(name = "type", nullable = false)
    public String type;

    @Column(name = "status", nullable = false)
    public String status;

    @Column(name = "payment_method", nullable = false)
    public String paymentMethod;

    @Column(name = "driver_id")
    public UUID driverId;

    @Column(name = "driver_name")
    public String driverName;

    @Column(name = "platform", nullable = false)
    public String platform;

    @Column(name = "external_order_id")
    public String externalOrderId;

    @Column(name = "subtotal", nullable = false)
    public BigDecimal subtotal;

    @Column(name = "delivery_cost", nullable = false)
    public BigDecimal deliveryCost;

    @Column(name = "total_value", nullable = false)
    public BigDecimal totalValue;

    @Column(name = "nfe_emitted", nullable = false)
    public boolean nfeEmitted;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @Column(name = "finished_at")
    public LocalDateTime finishedAt;
}
