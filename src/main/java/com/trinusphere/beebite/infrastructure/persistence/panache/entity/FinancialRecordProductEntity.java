package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA Entity — financial_record_product.
 */
@Entity
@Table(name = "financial_record_product")
public class FinancialRecordProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_record_id", nullable = false)
    public FinancialRecordEntity financialRecord;

    @Column(name = "product_id")
    public UUID productId;

    @Column(name = "product_name", nullable = false, length = 200)
    public String productName;

    @Column(name = "quantity", nullable = false)
    public int quantity;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    public BigDecimal price;
}
