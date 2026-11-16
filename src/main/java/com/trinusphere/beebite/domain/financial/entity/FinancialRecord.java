package com.trinusphere.beebite.domain.financial.entity;

import com.trinusphere.beebite.domain.financial.valueobject.FinancialRecordProduct;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialStatus;
import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import com.trinusphere.beebite.domain.shared.StoreGuard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity — Registro Financeiro. POJO puro, sem dependência de framework.
 *
 * <p>
 * Gerado automaticamente quando um pedido é finalizado. Contém um snapshot dos dados financeiros do pedido naquele
 * momento.
 */
public class FinancialRecord implements StoreGuard.StoreScoped {

    private UUID id;
    private final UUID storeId;
    private final UUID orderId;
    private LocalDate recordDate;
    private LocalTime recordTime;
    private final BigDecimal totalValue;
    private final PaymentMethod paymentMethod;
    private FinancialStatus status;
    private List<FinancialRecordProduct> products;
    private LocalDateTime createdAt;

    // ═══════════════════════════════════════════════════════════════════════
    // Construtor de criação — usado pelo CreateFinancialRecordUseCase
    // ═══════════════════════════════════════════════════════════════════════

    public FinancialRecord(UUID storeId, UUID orderId, BigDecimal totalValue, PaymentMethod paymentMethod,
            List<FinancialRecordProduct> products) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (orderId == null)
            throw new IllegalArgumentException("orderId é obrigatório.");
        if (totalValue == null)
            throw new IllegalArgumentException("totalValue é obrigatório.");
        if (paymentMethod == null)
            throw new IllegalArgumentException("paymentMethod é obrigatório.");

        this.storeId = storeId;
        this.orderId = orderId;
        this.recordDate = LocalDate.now();
        this.recordTime = LocalTime.now();
        this.totalValue = totalValue;
        this.paymentMethod = paymentMethod;
        this.status = FinancialStatus.CONCLUIDO;
        this.products = products != null ? products : new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Factory de reconstrução — usado EXCLUSIVAMENTE pelo Mapper (infra)
    // ═══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("java:S107")
    public static FinancialRecord reconstitute(UUID id, UUID storeId, UUID orderId, LocalDate recordDate,
            LocalTime recordTime, BigDecimal totalValue, PaymentMethod paymentMethod, FinancialStatus status,
            List<FinancialRecordProduct> products, LocalDateTime createdAt) {

        FinancialRecord record = new FinancialRecord(storeId, orderId, totalValue, paymentMethod, products);
        record.id = id;
        record.recordDate = recordDate;
        record.recordTime = recordTime;
        record.status = status;
        record.products = products != null ? products : new ArrayList<>();
        record.createdAt = createdAt;
        return record;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Métodos de comportamento
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Cancela o registro financeiro (ex: pedido estornado).
     */
    public void cancel() {
        this.status = FinancialStatus.CANCELADO;
    }

    /**
     * Marca como estornado.
     */
    public void refund() {
        this.status = FinancialStatus.ESTORNADO;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Getters
    // ═══════════════════════════════════════════════════════════════════════

    public UUID getId() {
        return id;
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public LocalTime getRecordTime() {
        return recordTime;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public FinancialStatus getStatus() {
        return status;
    }

    public List<FinancialRecordProduct> getProducts() {
        return products;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ── Setter para persistência ──
    public void setId(UUID id) {
        this.id = id;
    }
}
