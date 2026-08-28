package com.fidecode.biteflow.domain.order.entity;

import com.fidecode.biteflow.domain.order.valueobject.*;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root — Pedido. POJO puro, sem dependência de framework.
 *
 * <p>
 * Invariantes protegidos:
 * <ul>
 * <li>Status só muda via {@link #changeStatus(OrderStatus)} (valida transição)</li>
 * <li>Driver atribuído via {@link #assignDriver(UUID, String)} (atômico)</li>
 * <li>Customer info via {@link #updateCustomerInfo(String, String)} (atômico)</li>
 * <li>Items via {@link #addItem(OrderItem)} com recalculo automático</li>
 * </ul>
 *
 * <p>
 * Reconstrução a partir do banco: usar {@link #reconstitute}.
 */
public class Order implements StoreGuard.StoreScoped {

    private UUID id;
    private final UUID storeId;
    private Long version; // campo técnico — necessário para @Version do Hibernate round-trip
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

    // ═══════════════════════════════════════════════════════════════════════
    // Construtor de criação — usado pelos Use Cases
    // ═══════════════════════════════════════════════════════════════════════

    public Order(UUID storeId, OrderType type, PaymentMethod paymentMethod) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (type == null)
            throw new IllegalArgumentException("type é obrigatório.");
        if (paymentMethod == null)
            throw new IllegalArgumentException("paymentMethod é obrigatório.");
        this.storeId = storeId;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.ANALISE;
        this.platform = OrderPlatform.OWN;
        this.subtotal = BigDecimal.ZERO;
        this.deliveryCost = BigDecimal.ZERO;
        this.totalValue = BigDecimal.ZERO;
        this.nfeEmitted = false;
        this.items = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Factory de reconstrução — usado EXCLUSIVAMENTE pelo OrderMapper (infra)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Reconstitui um Order a partir do banco de dados. Não valida invariantes pois o objeto já foi validado na criação
     * original.
     */
    @SuppressWarnings("java:S107") // Muitos parâmetros é intencional — reconstrução completa
    public static Order reconstitute(UUID id, UUID storeId, String displayId, UUID customerId, String customerName,
            String customerAddress, OrderType type, OrderStatus status, PaymentMethod paymentMethod, UUID driverId,
            String driverName, OrderPlatform platform, String externalOrderId, BigDecimal subtotal,
            BigDecimal deliveryCost, BigDecimal totalValue, boolean nfeEmitted, List<OrderItem> items,
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime finishedAt) {

        return reconstitute(id, storeId, displayId, null, customerId, customerName, customerAddress, null, null, null,
                type, status, paymentMethod, driverId, driverName, platform, externalOrderId, null, null, null, null,
                null, null, subtotal, deliveryCost, totalValue, nfeEmitted, items, createdAt, updatedAt, finishedAt);
    }

    @SuppressWarnings("java:S107")
    public static Order reconstitute(UUID id, UUID storeId, String displayId, Long version, UUID customerId,
            String customerName, String customerAddress, String customerPhone, String customerAddressComplement,
            String customerAddressReference, OrderType type, OrderStatus status, PaymentMethod paymentMethod,
            UUID driverId, String driverName, OrderPlatform platform, String externalOrderId, String operation,
            String channel, String serviceType, String source, BigDecimal cashChangeFor, String notes,
            BigDecimal subtotal, BigDecimal deliveryCost, BigDecimal totalValue, boolean nfeEmitted,
            List<OrderItem> items, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime finishedAt) {

        Order order = new Order(storeId, type, paymentMethod);
        order.id = id;
        order.version = version;
        order.displayId = displayId;
        order.customerId = customerId;
        order.customerName = customerName;
        order.customerAddress = customerAddress;
        order.customerPhone = customerPhone;
        order.customerAddressComplement = customerAddressComplement;
        order.customerAddressReference = customerAddressReference;
        order.status = status;
        order.driverId = driverId;
        order.driverName = driverName;
        order.platform = platform;
        order.externalOrderId = externalOrderId;
        order.operation = operation;
        order.channel = channel;
        order.serviceType = serviceType;
        order.source = source;
        order.cashChangeFor = cashChangeFor;
        order.notes = notes;
        order.subtotal = subtotal;
        order.deliveryCost = deliveryCost;
        order.totalValue = totalValue;
        order.nfeEmitted = nfeEmitted;
        order.items = items != null ? items : new ArrayList<>();
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;
        order.finishedAt = finishedAt;
        return order;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Métodos de comportamento — encapsulam invariantes
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Verifica se o pedido pode ser editado. Pedidos em trânsito (EM_ROTA), concluídos (FINALIZADO) ou cancelados
     * (CANCELADO) são imutáveis — qualquer edição geraria inconsistência operacional.
     */
    public boolean isEditable() {
        return this.status == OrderStatus.ANALISE || this.status == OrderStatus.PREPARO;
    }

    /**
     * Adiciona um item ao pedido e recalcula totais.
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotals();
    }

    /**
     * Recalcula subtotal e totalValue a partir dos itens. totalValue = subtotal + deliveryCost
     */
    public void recalculateTotals() {
        this.subtotal = items.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalValue = subtotal.add(deliveryCost != null ? deliveryCost : BigDecimal.ZERO);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Altera o status com atualização automática de timestamps. Não valida transição aqui — a validação é
     * responsabilidade do {@code OrderDomainService} que conhece as regras de fluxo.
     */
    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        if (newStatus == OrderStatus.FINALIZADO) {
            this.finishedAt = LocalDateTime.now();
        }
    }

    /**
     * Atribui entregador de forma atômica (id + nome juntos).
     */
    public void assignDriver(UUID driverId, String driverName) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Atualiza informações do cliente de forma atômica.
     */
    public void updateCustomerInfo(String customerName, String customerAddress) {
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Define o contexto operacional do pedido (campos de integração iFood/plataformas).
     */
    public void defineContext(String operation, String channel, String serviceType, String source) {
        this.operation = operation;
        this.channel = channel;
        this.serviceType = serviceType;
        this.source = source;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Atualiza o customerId (ao vincular um cliente existente).
     */
    public void linkCustomer(UUID customerId) {
        this.customerId = customerId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Atualiza o tipo do pedido (ex: BALCAO → DELIVERY).
     */
    public void changeType(OrderType type) {
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Atualiza o método de pagamento.
     */
    public void changePaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Define o custo de entrega e recalcula o total.
     */
    public void setDeliveryCost(BigDecimal deliveryCost) {
        this.deliveryCost = deliveryCost;
        recalculateTotals();
    }

    /**
     * Alias semântico de {@link #setDeliveryCost} — define o custo de entrega e recalcula o total.
     */
    public void changeDeliveryCost(BigDecimal deliveryCost) {
        setDeliveryCost(deliveryCost);
    }

    /**
     * Atualiza informações de contato do cliente (telefone, complemento, referência) de forma atômica.
     */
    public void updateCustomerContact(String phone, String addressComplement, String addressReference) {
        this.customerPhone = phone;
        this.customerAddressComplement = addressComplement;
        this.customerAddressReference = addressReference;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Define o valor para troco.
     */
    public void defineCashChangeFor(BigDecimal cashChangeFor) {
        this.cashChangeFor = cashChangeFor;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Atualiza as observações do pedido.
     */
    public void updateNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Define o displayId (gerado na criação).
     */
    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }

    /**
     * Define o ID (atribuído pela persistência).
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /** Setter para integração iFood — define a plataforma de origem. */
    public void setPlatform(OrderPlatform platform) {
        this.platform = platform;
        this.updatedAt = LocalDateTime.now();
    }

    /** Setter para integração iFood — define o ID externo do pedido. */
    public void setExternalOrderId(String externalOrderId) {
        this.externalOrderId = externalOrderId;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsNfeEmitted() {
        this.nfeEmitted = true;
        this.updatedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Getters — acesso de leitura
    // ═══════════════════════════════════════════════════════════════════════

    public UUID getId() {
        return id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public Long getVersion() {
        return version;
    }

    public String getDisplayId() {
        return displayId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerAddressComplement() {
        return customerAddressComplement;
    }

    public String getCustomerAddressReference() {
        return customerAddressReference;
    }

    public OrderType getType() {
        return type;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public OrderPlatform getPlatform() {
        return platform;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public String getOperation() {
        return operation;
    }

    public String getChannel() {
        return channel;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getSource() {
        return source;
    }

    public BigDecimal getCashChangeFor() {
        return cashChangeFor;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDeliveryCost() {
        return deliveryCost;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public boolean isNfeEmitted() {
        return nfeEmitted;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCustomerAddressComplement(String customerAddressComplement) {
        this.customerAddressComplement = customerAddressComplement;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCustomerAddressReference(String customerAddressReference) {
        this.customerAddressReference = customerAddressReference;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCashChangeFor(BigDecimal cashChangeFor) {
        this.cashChangeFor = cashChangeFor;
        this.updatedAt = LocalDateTime.now();
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }
}
