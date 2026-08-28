package com.fidecode.biteflow.domain.order.entity;

import com.fidecode.biteflow.domain.order.valueobject.*;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Aggregate Root — Pedido. POJO puro, sem dependência de framework. */
public class Order implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private Long version;
    private String displayId;
    private UUID customerId;
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String customerAddressComplement;
    private String customerAddressReference;
    private OrderType type;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private UUID driverId;
    private String driverName;
    private OrderPlatform platform;
    private String externalOrderId;
    private String operation;
    private String channel;
    private String serviceType;
    private String source;
    private BigDecimal cashChangeFor;
    private String notes;
    private BigDecimal subtotal;
    private BigDecimal deliveryCost;
    private BigDecimal totalValue;
    private boolean nfeEmitted;
    private List<OrderItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime finishedAt;

    public Order(UUID storeId, OrderType type, PaymentMethod paymentMethod) {
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (type == null) throw new IllegalArgumentException("type é obrigatório.");
        if (paymentMethod == null) throw new IllegalArgumentException("paymentMethod é obrigatório.");
        this.storeId = storeId;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.ANALISE;
        this.platform = OrderPlatform.OWN;
        this.operation = type == OrderType.DELIVERY ? "DELIVERY" : "SALAO";
        this.channel = "PDV";
        this.serviceType = type == OrderType.DELIVERY ? "ENTREGA" : "CONSUMO_LOCAL";
        this.source = "PDV_CAIXA";
        this.subtotal = BigDecimal.ZERO;
        this.deliveryCost = BigDecimal.ZERO;
        this.totalValue = BigDecimal.ZERO;
        this.nfeEmitted = false;
        this.items = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Assinatura legada — preservada para testes e chamadas existentes.
    @SuppressWarnings("java:S107")
    public static Order reconstitute(
            UUID id, UUID storeId, String displayId,
            UUID customerId, String customerName, String customerAddress,
            OrderType type, OrderStatus status, PaymentMethod paymentMethod,
            UUID driverId, String driverName,
            OrderPlatform platform, String externalOrderId,
            BigDecimal subtotal, BigDecimal deliveryCost, BigDecimal totalValue,
            boolean nfeEmitted, List<OrderItem> items,
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime finishedAt) {
        return reconstitute(id, storeId, displayId, null, customerId, customerName, customerAddress,
            null, null, null, type, status, paymentMethod, driverId, driverName, platform, externalOrderId,
            type == OrderType.DELIVERY ? "DELIVERY" : "SALAO",
            platform == OrderPlatform.IFOOD ? "IFOOD" : platform == OrderPlatform.WHATSAPP ? "WHATSAPP" : "PDV",
            type == OrderType.DELIVERY ? "ENTREGA" : "CONSUMO_LOCAL",
            platform == OrderPlatform.IFOOD ? "INTEGRACAO_IFOOD" : platform == OrderPlatform.WHATSAPP ? "ATENDIMENTO_WHATSAPP" : "PDV_CAIXA",
            null, null, subtotal, deliveryCost, totalValue, nfeEmitted, items, createdAt, updatedAt, finishedAt);
    }

    // Assinatura legada com version — preservada.
    @SuppressWarnings("java:S107")
    public static Order reconstitute(
            UUID id, UUID storeId, String displayId, Long version,
            UUID customerId, String customerName, String customerAddress,
            OrderType type, OrderStatus status, PaymentMethod paymentMethod,
            UUID driverId, String driverName,
            OrderPlatform platform, String externalOrderId,
            BigDecimal subtotal, BigDecimal deliveryCost, BigDecimal totalValue,
            boolean nfeEmitted, List<OrderItem> items,
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime finishedAt) {
        return reconstitute(id, storeId, displayId, version, customerId, customerName, customerAddress,
            null, null, null, type, status, paymentMethod, driverId, driverName, platform, externalOrderId,
            type == OrderType.DELIVERY ? "DELIVERY" : "SALAO",
            platform == OrderPlatform.IFOOD ? "IFOOD" : platform == OrderPlatform.WHATSAPP ? "WHATSAPP" : "PDV",
            type == OrderType.DELIVERY ? "ENTREGA" : "CONSUMO_LOCAL",
            platform == OrderPlatform.IFOOD ? "INTEGRACAO_IFOOD" : platform == OrderPlatform.WHATSAPP ? "ATENDIMENTO_WHATSAPP" : "PDV_CAIXA",
            null, null, subtotal, deliveryCost, totalValue, nfeEmitted, items, createdAt, updatedAt, finishedAt);
    }

    @SuppressWarnings("java:S107")
    public static Order reconstitute(
            UUID id, UUID storeId, String displayId, Long version,
            UUID customerId, String customerName, String customerAddress,
            String customerPhone, String customerAddressComplement, String customerAddressReference,
            OrderType type, OrderStatus status, PaymentMethod paymentMethod,
            UUID driverId, String driverName,
            OrderPlatform platform, String externalOrderId,
            String operation, String channel, String serviceType, String source,
            BigDecimal cashChangeFor, String notes,
            BigDecimal subtotal, BigDecimal deliveryCost, BigDecimal totalValue,
            boolean nfeEmitted, List<OrderItem> items,
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime finishedAt) {
        Order order = new Order(storeId, type, paymentMethod);
        order.id = id; order.version = version; order.displayId = displayId; order.customerId = customerId;
        order.customerName = customerName; order.customerAddress = customerAddress; order.customerPhone = customerPhone;
        order.customerAddressComplement = customerAddressComplement; order.customerAddressReference = customerAddressReference;
        order.status = status; order.driverId = driverId; order.driverName = driverName; order.platform = platform;
        order.externalOrderId = externalOrderId; order.operation = operation; order.channel = channel;
        order.serviceType = serviceType; order.source = source; order.cashChangeFor = cashChangeFor; order.notes = notes;
        order.subtotal = subtotal; order.deliveryCost = deliveryCost; order.totalValue = totalValue; order.nfeEmitted = nfeEmitted;
        order.items = items != null ? items : new ArrayList<>(); order.createdAt = createdAt; order.updatedAt = updatedAt; order.finishedAt = finishedAt;
        return order;
    }

    public boolean isEditable() { return this.status == OrderStatus.ANALISE || this.status == OrderStatus.PREPARO; }
    public void addItem(OrderItem item) { this.items.add(item); recalculateTotals(); }
    public void recalculateTotals() {
        this.subtotal = items.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalValue = subtotal.add(deliveryCost != null ? deliveryCost : BigDecimal.ZERO);
        this.updatedAt = LocalDateTime.now();
    }
    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus; this.updatedAt = LocalDateTime.now();
        if (newStatus == OrderStatus.FINALIZADO) this.finishedAt = LocalDateTime.now();
    }
    public void assignDriver(UUID driverId, String driverName) { this.driverId = driverId; this.driverName = driverName; this.updatedAt = LocalDateTime.now(); }
    public void updateCustomerInfo(String customerName, String customerAddress) { this.customerName = customerName; this.customerAddress = customerAddress; this.updatedAt = LocalDateTime.now(); }
    public void updateCustomerContact(String phone, String complement, String reference) {
        this.customerPhone = phone; this.customerAddressComplement = complement; this.customerAddressReference = reference; this.updatedAt = LocalDateTime.now();
    }
    public void linkCustomer(UUID customerId) { this.customerId = customerId; this.updatedAt = LocalDateTime.now(); }
    public void changeType(OrderType type) { this.type = type; this.updatedAt = LocalDateTime.now(); }
    public void changePaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; this.updatedAt = LocalDateTime.now(); }
    public void changeDeliveryCost(BigDecimal deliveryCost) { this.deliveryCost = deliveryCost != null ? deliveryCost : BigDecimal.ZERO; recalculateTotals(); }
    public void setDisplayId(String displayId) { this.displayId = displayId; }
    public void setId(UUID id) { this.id = id; }
    public void markAsNfeEmitted() { this.nfeEmitted = true; this.updatedAt = LocalDateTime.now(); }
    public void defineContext(String operation, String channel, String serviceType, String source) {
        if (operation != null && !operation.isBlank()) this.operation = operation.toUpperCase();
        if (channel != null && !channel.isBlank()) this.channel = channel.toUpperCase();
        if (serviceType != null && !serviceType.isBlank()) this.serviceType = serviceType.toUpperCase();
        if (source != null && !source.isBlank()) this.source = source.toUpperCase();
        this.platform = switch (this.channel) { case "IFOOD" -> OrderPlatform.IFOOD; case "WHATSAPP" -> OrderPlatform.WHATSAPP; default -> OrderPlatform.OWN; };
        this.updatedAt = LocalDateTime.now();
    }
    public void defineCashChangeFor(BigDecimal cashChangeFor) { this.cashChangeFor = cashChangeFor; }
    public void updateNotes(String notes) { this.notes = notes; }
    public void linkExternalOrder(String externalOrderId) { this.externalOrderId = externalOrderId; }

    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public Long getVersion() { return version; }
    public String getDisplayId() { return displayId; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerAddress() { return customerAddress; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerAddressComplement() { return customerAddressComplement; }
    public String getCustomerAddressReference() { return customerAddressReference; }
    public OrderType getType() { return type; }
    public OrderStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public UUID getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public OrderPlatform getPlatform() { return platform; }
    public String getExternalOrderId() { return externalOrderId; }
    public String getOperation() { return operation; }
    public String getChannel() { return channel; }
    public String getServiceType() { return serviceType; }
    public String getSource() { return source; }
    public BigDecimal getCashChangeFor() { return cashChangeFor; }
    public String getNotes() { return notes; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDeliveryCost() { return deliveryCost; }
    public BigDecimal getTotalValue() { return totalValue; }
    public boolean isNfeEmitted() { return nfeEmitted; }
    public List<OrderItem> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
}
