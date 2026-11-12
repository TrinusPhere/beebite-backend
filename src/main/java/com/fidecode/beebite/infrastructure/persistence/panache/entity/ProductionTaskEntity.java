package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "production_task")
public class ProductionTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "order_id", nullable = false)
    public UUID orderId;

    @Column(name = "order_item_id", nullable = false)
    public UUID orderItemId;

    @Column(name = "sector_id")
    public UUID sectorId;

    @Column(name = "display_order_id", length = 50)
    public String displayOrderId;

    @Column(name = "order_type", length = 30)
    public String orderType;

    @Column(name = "order_channel", length = 40)
    public String orderChannel;

    @Column(name = "customer_name", length = 150)
    public String customerName;

    @Column(name = "item_name", nullable = false, length = 200)
    public String itemName;

    @Column(nullable = false)
    public int quantity;

    @Column(columnDefinition = "TEXT")
    public String notes;

    @Column(name = "modifiers_summary", columnDefinition = "TEXT")
    public String modifiersSummary;

    @Column(name = "combo_name", length = 200)
    public String comboName;

    @Column(nullable = false, length = 20)
    public String status;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "canceled_at")
    public LocalDateTime canceledAt;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}

