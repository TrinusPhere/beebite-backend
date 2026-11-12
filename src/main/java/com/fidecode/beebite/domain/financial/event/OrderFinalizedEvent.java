package com.fidecode.beebite.domain.financial.event;

import com.fidecode.beebite.domain.order.valueobject.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Evento de domínio — Pedido Finalizado. POJO puro, sem dependência de framework.
 *
 * <p>
 * Disparado quando um pedido transiciona para status FINALIZADO. Contém snapshot dos dados necessários para criar o
 * FinancialRecord e atualizar contadores de driver/customer.
 */
public class OrderFinalizedEvent {

    private final UUID orderId;
    private final UUID storeId;
    private final BigDecimal totalValue;
    private final PaymentMethod paymentMethod;
    private final UUID customerId;
    private final UUID driverId;
    private final List<ProductSnapshot> products;

    public OrderFinalizedEvent(UUID orderId, UUID storeId, BigDecimal totalValue, PaymentMethod paymentMethod,
            UUID customerId, UUID driverId, List<ProductSnapshot> products) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.totalValue = totalValue;
        this.paymentMethod = paymentMethod;
        this.customerId = customerId;
        this.driverId = driverId;
        this.products = products;
    }

    // ── Getters ──
    public UUID getOrderId() {
        return orderId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public List<ProductSnapshot> getProducts() {
        return products;
    }

    /**
     * Snapshot imutável de um produto no momento da finalização.
     */
    public record ProductSnapshot(UUID productId, String productName, int quantity, BigDecimal price) {
    }
}
