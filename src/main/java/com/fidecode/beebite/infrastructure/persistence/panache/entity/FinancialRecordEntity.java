package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity — financial_record.
 */
@Entity
@Table(name = "financial_record")
public class FinancialRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "order_id", nullable = false)
    public UUID orderId;

    @Column(name = "record_date", nullable = false)
    public LocalDate recordDate;

    @Column(name = "record_time", nullable = false)
    public LocalTime recordTime;

    @Column(name = "total_value", nullable = false, precision = 12, scale = 2)
    public BigDecimal totalValue;

    @Column(name = "payment_method", nullable = false, length = 30)
    public String paymentMethod;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @OneToMany(mappedBy = "financialRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<FinancialRecordProductEntity> products = new ArrayList<>();
}
