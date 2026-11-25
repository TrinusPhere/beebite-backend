package com.trinusphere.beebite.infrastructure.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trinusphere.beebite.domain.cash.entity.CashMovement;
import com.trinusphere.beebite.domain.cash.entity.CashRegister;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialRecordProduct;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.entity.OrderItem;
import com.trinusphere.beebite.domain.payment.entity.Payment;
import com.trinusphere.beebite.domain.store.entity.Store;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@ApplicationScoped
public class EdgeSyncPayloadSerializer {

    private final ObjectMapper objectMapper;

    public EdgeSyncPayloadSerializer() {
        this(new ObjectMapper());
    }

    @Inject
    public EdgeSyncPayloadSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serializeOrder(Order order) {
        ObjectNode node = objectMapper.createObjectNode();
        putText(node, "displayId", order.getDisplayId() != null ? order.getDisplayId() : "#EDGE");
        putUuid(node, "customerId", order.getCustomerId());
        putText(node, "customerName", order.getCustomerName());
        putText(node, "customerAddress", order.getCustomerAddress());
        putText(node, "customerPhone", order.getCustomerPhone());
        putText(node, "customerAddressComplement", order.getCustomerAddressComplement());
        putText(node, "customerAddressReference", order.getCustomerAddressReference());
        putText(node, "type", order.getType() != null ? order.getType().name() : "BALCAO");
        putText(node, "status", order.getStatus() != null ? order.getStatus().name() : "ANALISE");
        putText(node, "paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "PIX");
        putUuid(node, "driverId", order.getDriverId());
        putText(node, "driverName", order.getDriverName());
        putText(node, "platform", order.getPlatform() != null ? order.getPlatform().name() : "OWN");
        putText(node, "externalOrderId", order.getExternalOrderId());
        putText(node, "operation", order.getOperation() != null ? order.getOperation() : "BALCAO");
        putText(node, "channel", order.getChannel() != null ? order.getChannel() : "PDV");
        putText(node, "serviceType", order.getServiceType() != null ? order.getServiceType() : "BALCAO");
        putText(node, "source", order.getSource() != null ? order.getSource() : "SISTEMA");
        putDecimal(node, "cashChangeFor", order.getCashChangeFor());
        putText(node, "notes", order.getNotes());
        putDecimal(node, "subtotal", defaultZero(order.getSubtotal()));
        putDecimal(node, "deliveryCost", defaultZero(order.getDeliveryCost()));
        putDecimal(node, "totalAmount", defaultZero(order.getTotalValue()));
        putDecimal(node, "totalValue", defaultZero(order.getTotalValue()));
        node.put("nfeEmitted", order.isNfeEmitted());
        putDateTime(node, "createdAt", order.getCreatedAt());
        putDateTime(node, "updatedAt", order.getUpdatedAt());
        putDateTime(node, "finishedAt", order.getFinishedAt());

        ArrayNode itemsArray = node.putArray("items");
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                ObjectNode itemNode = itemsArray.addObject();
                putUuid(itemNode, "id", item.getId() != null ? item.getId() : UUID.randomUUID());
                putUuid(itemNode, "productId", item.getProductId());
                putText(itemNode, "productName", item.getProductName());
                itemNode.put("quantity", item.getQuantity());
                putDecimal(itemNode, "unitPrice", defaultZero(item.getUnitPrice()));
                putDecimal(itemNode, "extraPrice", defaultZero(item.getExtraPrice()));
                putDecimal(itemNode, "totalPrice", defaultZero(item.getTotalPrice()));
                putText(itemNode, "observation", item.getObservation());
            }
        }
        return toJson(node);
    }

    public String serializeFinancialRecord(FinancialRecord record) {
        ObjectNode node = objectMapper.createObjectNode();
        putUuid(node, "orderId", record.getOrderId());
        putDate(node, "recordDate", record.getRecordDate());
        putTime(node, "recordTime", record.getRecordTime());
        putDecimal(node, "totalValue", defaultZero(record.getTotalValue()));
        putText(node, "paymentMethod", record.getPaymentMethod() != null ? record.getPaymentMethod().name() : "PIX");
        putText(node, "status", record.getStatus() != null ? record.getStatus().name() : "CONCLUIDO");
        putDateTime(node, "createdAt", record.getCreatedAt());

        ArrayNode productsArray = node.putArray("products");
        if (record.getProducts() != null) {
            for (FinancialRecordProduct p : record.getProducts()) {
                ObjectNode pNode = productsArray.addObject();
                putUuid(pNode, "id", p.getId() != null ? p.getId() : UUID.randomUUID());
                putUuid(pNode, "productId", p.getProductId());
                putText(pNode, "productName", p.getProductName());
                pNode.put("quantity", p.getQuantity());
                putDecimal(pNode, "price", defaultZero(p.getPrice()));
            }
        }
        return toJson(node);
    }

    public String serializeCashRegister(CashRegister register) {
        ObjectNode node = objectMapper.createObjectNode();
        putText(node, "name", register.getName());
        node.put("active", register.isActive());
        putDateTime(node, "createdAt", register.getCreatedAt());
        putDateTime(node, "updatedAt", register.getUpdatedAt());
        putDateTime(node, "deletedAt", register.getDeletedAt());
        return toJson(node);
    }

    public String serializeCashSession(CashSession session) {
        ObjectNode node = objectMapper.createObjectNode();
        putUuid(node, "cashRegisterId", session.getCashRegisterId());
        putUuid(node, "userId", session.getUserId());
        putText(node, "status", session.getStatus() != null ? session.getStatus().name() : "OPEN");
        putDateTime(node, "openedAt", session.getOpenedAt());
        putDateTime(node, "closedAt", session.getClosedAt());
        putDecimal(node, "initialAmount", defaultZero(session.getInitialAmount()));
        putDecimal(node, "expectedAmount", session.getExpectedAmount());
        putDecimal(node, "countedAmount", session.getCountedAmount());
        putDecimal(node, "differenceAmount", session.getDifferenceAmount());
        putText(node, "closingNotes", session.getClosingNotes());
        putDateTime(node, "createdAt", session.getCreatedAt());
        putDateTime(node, "updatedAt", session.getUpdatedAt());
        return toJson(node);
    }

    public String serializeCashMovement(CashMovement movement) {
        ObjectNode node = objectMapper.createObjectNode();
        putUuid(node, "cashSessionId", movement.getCashSessionId());
        putUuid(node, "userId", movement.getUserId());
        putText(node, "type", movement.getType() != null ? movement.getType().name() : "SUPRIMENTO");
        putDecimal(node, "amount", defaultZero(movement.getAmount()));
        putText(node, "reason", movement.getReason());
        putDateTime(node, "createdAt", movement.getCreatedAt());
        return toJson(node);
    }

    public String serializePayment(Payment payment) {
        ObjectNode node = objectMapper.createObjectNode();
        putUuid(node, "orderId", payment.getOrderId());
        putUuid(node, "cashSessionId", payment.getCashSessionId());
        putUuid(node, "driverId", payment.getDriverId());
        putText(node, "method", payment.getMethod() != null ? payment.getMethod().name() : "PIX");
        putDecimal(node, "amount", defaultZero(payment.getAmount()));
        putDecimal(node, "changeFor", payment.getChangeFor());
        putText(node, "status", payment.getStatus() != null ? payment.getStatus().name() : "CONFIRMED");
        putText(node, "collectedBy", payment.getCollectedBy() != null ? payment.getCollectedBy().name() : "CAIXA");
        putText(node, "notes", payment.getNotes());
        putDateTime(node, "paidAt", payment.getPaidAt());
        putDateTime(node, "createdAt", payment.getCreatedAt());
        return toJson(node);
    }

    public String serializeStoreStatus(Store store) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("open", store.isOpen());
        putText(node, "openingHours", store.getOpeningHours());
        return toJson(node);
    }

    private BigDecimal defaultZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private void putText(ObjectNode node, String field, String value) {
        if (value != null) {
            node.put(field, value);
        } else {
            node.putNull(field);
        }
    }

    private void putUuid(ObjectNode node, String field, UUID value) {
        if (value != null) {
            node.put(field, value.toString());
        } else {
            node.putNull(field);
        }
    }

    private void putDecimal(ObjectNode node, String field, BigDecimal value) {
        if (value != null) {
            node.put(field, value.toPlainString());
        } else {
            node.putNull(field);
        }
    }

    private void putDateTime(ObjectNode node, String field, LocalDateTime value) {
        if (value != null) {
            node.put(field, value.toString());
        } else {
            node.putNull(field);
        }
    }

    private void putDate(ObjectNode node, String field, LocalDate value) {
        if (value != null) {
            node.put(field, value.toString());
        } else {
            node.putNull(field);
        }
    }

    private void putTime(ObjectNode node, String field, LocalTime value) {
        if (value != null) {
            node.put(field, value.toString());
        } else {
            node.putNull(field);
        }
    }

    private String toJson(ObjectNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar evento de sincronização Edge.", e);
        }
    }
}
