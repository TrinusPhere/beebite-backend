package com.trinusphere.beebite.domain.platform.entity;

import com.trinusphere.beebite.domain.platform.valueobject.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class SaasInvoice {
    private UUID id;
    private final UUID contractId;
    private final UUID tenantId;
    private final String referenceMonth;
    private final String description;
    private final BigDecimal amount;
    private final LocalDate dueDate;
    private Instant paidAt;
    private String paymentMethod;
    private InvoiceStatus status;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;

    public SaasInvoice(
            UUID contractId,
            UUID tenantId,
            String referenceMonth,
            String description,
            BigDecimal amount,
            LocalDate dueDate) {
        validate(contractId, tenantId, referenceMonth, description, amount, dueDate);
        this.contractId = contractId;
        this.tenantId = tenantId;
        this.referenceMonth = referenceMonth.trim();
        this.description = description.trim();
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = InvoiceStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private SaasInvoice(
            UUID id,
            UUID contractId,
            UUID tenantId,
            String referenceMonth,
            String description,
            BigDecimal amount,
            LocalDate dueDate,
            Instant paidAt,
            String paymentMethod,
            InvoiceStatus status,
            String notes,
            Instant createdAt,
            Instant updatedAt) {
        validate(contractId, tenantId, referenceMonth, description, amount, dueDate);
        this.id = id;
        this.contractId = contractId;
        this.tenantId = tenantId;
        this.referenceMonth = referenceMonth.trim();
        this.description = description.trim();
        this.amount = amount;
        this.dueDate = dueDate;
        this.paidAt = paidAt;
        this.paymentMethod = paymentMethod;
        this.status = status != null ? status : InvoiceStatus.PENDING;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static SaasInvoice reconstitute(
            UUID id,
            UUID contractId,
            UUID tenantId,
            String referenceMonth,
            String description,
            BigDecimal amount,
            LocalDate dueDate,
            Instant paidAt,
            String paymentMethod,
            InvoiceStatus status,
            String notes,
            Instant createdAt,
            Instant updatedAt) {
        return new SaasInvoice(
                id, contractId, tenantId, referenceMonth, description, amount, dueDate, paidAt, paymentMethod,
                status, notes, createdAt, updatedAt
        );
    }

    private static void validate(
            UUID contractId,
            UUID tenantId,
            String referenceMonth,
            String description,
            BigDecimal amount,
            LocalDate dueDate) {
        if (contractId == null) {
            throw new IllegalArgumentException("contractId é obrigatório.");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId é obrigatório.");
        }
        if (referenceMonth == null || referenceMonth.isBlank()) {
            throw new IllegalArgumentException("referenceMonth é obrigatório.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description é obrigatório.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount deve ser maior que zero.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("dueDate é obrigatório.");
        }
    }

    public void markPaid(String paymentMethod, Instant paidAt, String notes) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("paymentMethod é obrigatório.");
        }
        this.status = InvoiceStatus.PAID;
        this.paymentMethod = paymentMethod.trim().toUpperCase();
        this.paidAt = paidAt != null ? paidAt : Instant.now();
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public void markOverdue() {
        if (this.status == InvoiceStatus.PENDING) {
            this.status = InvoiceStatus.OVERDUE;
            this.updatedAt = Instant.now();
        }
    }

    public void cancel(String notes) {
        this.status = InvoiceStatus.CANCELLED;
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getContractId() {
        return contractId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getReferenceMonth() {
        return referenceMonth;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

