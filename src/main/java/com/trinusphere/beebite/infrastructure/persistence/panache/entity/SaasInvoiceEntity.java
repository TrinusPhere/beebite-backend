package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "saas_invoice")
public class SaasInvoiceEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "contract_id", nullable = false)
    public UUID contractId;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "reference_month", nullable = false, length = 7)
    public String referenceMonth;

    @Column(name = "description", nullable = false, length = 200)
    public String description;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    public BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    public LocalDate dueDate;

    @Column(name = "paid_at")
    public Instant paidAt;

    @Column(name = "payment_method", length = 30)
    public String paymentMethod;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    public String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}

