package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "orders")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Version
    public Long version;

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

    @Column(name = "customer_phone")
    public String customerPhone;

    @Column(name = "customer_address_complement")
    public String customerAddressComplement;

    @Column(name = "customer_address_reference")
    public String customerAddressReference;

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

    @Column(name = "operation", nullable = false)
    public String operation;

    @Column(name = "channel", nullable = false)
    public String channel;

    @Column(name = "service_type", nullable = false)
    public String serviceType;

    @Column(name = "source", nullable = false)
    public String source;

    @Column(name = "cash_change_for")
    public BigDecimal cashChangeFor;

    @Column(name = "notes", columnDefinition = "TEXT")
    public String notes;

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

    @org.hibernate.annotations.BatchSize(size = 50)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OrderItemEntity> items = new ArrayList<>();

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}
