package com.trinusphere.beebite.infrastructure.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trinusphere.beebite.domain.cash.entity.CashMovement;
import com.trinusphere.beebite.domain.cash.entity.CashRegister;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.catalog.entity.Additional;
import com.trinusphere.beebite.domain.catalog.entity.Ingredient;
import com.trinusphere.beebite.domain.catalog.entity.ModifierGroup;
import com.trinusphere.beebite.domain.catalog.entity.ModifierOption;
import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.entity.ProductCategory;
import com.trinusphere.beebite.domain.catalog.entity.TechnicalSheetItem;
import com.trinusphere.beebite.domain.catalog.valueobject.ChannelPrice;
import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialRecordProduct;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.entity.OrderItem;
import com.trinusphere.beebite.domain.payment.entity.Payment;
import com.trinusphere.beebite.domain.store.entity.Store;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;
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

    public String serializeCatalogCategory(ProductCategory category, Instant updatedAt, LocalDateTime deletedAt) {
        ObjectNode node = objectMapper.createObjectNode();
        putUuid(node, "catalogId", category.getCatalogId());
        putText(node, "name", category.getName());
        putText(node, "status", category.getStatus() != null ? category.getStatus().name() : "AVAILABLE");
        node.put("displayOrder", category.getDisplayOrder());
        ArrayNode modIds = node.putArray("defaultModifierGroupIds");
        if (category.getDefaultModifierGroupIds() != null) {
            for (UUID id : category.getDefaultModifierGroupIds()) {
                if (id != null) {
                    modIds.add(id.toString());
                }
            }
        }
        putInstant(node, "updatedAt", updatedAt != null ? updatedAt : Instant.now());
        putDateTime(node, "deletedAt", deletedAt);
        return toJson(node);
    }

    public String serializeCatalogModifierGroup(ModifierGroup group, Instant updatedAt, LocalDateTime deletedAt) {
        ObjectNode node = objectMapper.createObjectNode();
        putUuid(node, "catalogId", group.getCatalogId());
        putText(node, "name", group.getName());
        putText(node, "type", group.getType() != null ? group.getType().name() : "RADIO");
        putText(node, "semanticType", group.getSemanticType() != null ? group.getSemanticType().name() : null);
        node.put("required", group.isRequired());
        node.put("minSelections", group.getMinSelections());
        node.put("maxSelections", group.getMaxSelections());
        putText(node, "icon", group.getIcon());
        ArrayNode optionsArr = node.putArray("options");
        if (group.getOptions() != null) {
            for (ModifierOption opt : group.getOptions()) {
                ObjectNode oNode = optionsArr.addObject();
                putUuid(oNode, "id", opt.getId() != null ? opt.getId() : UUID.randomUUID());
                putText(oNode, "name", opt.getName());
                putDecimal(oNode, "price", defaultZero(opt.getPrice()));
                oNode.put("sortOrder", opt.getSortOrder());
                putUuid(oNode, "linkedProductId", opt.getLinkedProductId());
            }
        }
        putInstant(node, "updatedAt", updatedAt != null ? updatedAt : Instant.now());
        putDateTime(node, "deletedAt", deletedAt);
        return toJson(node);
    }

    public String serializeCatalogAdditional(Additional additional, Instant updatedAt, LocalDateTime deletedAt) {
        ObjectNode node = objectMapper.createObjectNode();
        putUuid(node, "catalogId", additional.getCatalogId());
        putText(node, "name", additional.getName());
        putDecimal(node, "price", defaultZero(additional.getPrice()));
        putText(node, "category", additional.getCategory());
        node.put("active", additional.isActive());
        putInstant(node, "updatedAt", updatedAt != null ? updatedAt : Instant.now());
        putDateTime(node, "deletedAt", deletedAt);
        return toJson(node);
    }

    public String serializeCatalogIngredient(Ingredient ingredient, Instant updatedAt, LocalDateTime deletedAt) {
        ObjectNode node = objectMapper.createObjectNode();
        putUuid(node, "catalogId", ingredient.getCatalogId());
        putText(node, "name", ingredient.getName());
        putText(node, "unit", ingredient.getUnit() != null ? ingredient.getUnit().name() : "UN");
        putDecimal(node, "costPrice", defaultZero(ingredient.getCostPrice()));
        node.put("active", ingredient.isActive());
        putDecimal(node, "stock", defaultZero(ingredient.getStock()));
        putDecimal(node, "minStock", defaultZero(ingredient.getMinStock()));
        putInstant(node, "updatedAt", updatedAt != null ? updatedAt : Instant.now());
        putDateTime(node, "deletedAt", deletedAt);
        return toJson(node);
    }

    public String serializeCatalogProduct(Product product, Instant updatedAt, LocalDateTime deletedAt) {
        ObjectNode node = objectMapper.createObjectNode();
        putUuid(node, "catalogId", product.getCatalogId());
        putText(node, "name", product.getName());
        putUuid(node, "categoryId", product.getCategoryId());
        putText(node, "description", product.getDescription());
        putDecimal(node, "price", defaultZero(product.getPrice()));
        putDecimal(node, "estimatedCost", product.getEstimatedCost());
        putText(node, "imageUrl", product.getImageUrl());
        putText(node, "status", product.getGlobalStatus() != null ? product.getGlobalStatus().name() : "AVAILABLE");
        putText(node, "localStatus", product.getLocalStatus() != null ? product.getLocalStatus().name() : "AVAILABLE");
        node.put("stock", product.getStock());
        putUuid(node, "productionSectorId", product.getProductionSectorId());
        putText(node, "defaultSectorCode", product.getDefaultSectorCode());
        putText(node, "externalCode", product.getExternalCode());
        putText(node, "catalogItemType", product.getCatalogItemType() != null ? product.getCatalogItemType().name() : "DEFAULT");
        putText(node, "productType", product.getProductType() != null ? product.getProductType().name() : "PRODUCED");
        node.put("trackStock", product.isTrackStock());
        putText(node, "saleUnit", product.getSaleUnit() != null ? product.getSaleUnit().name() : "UN");
        node.put("availableDigitalMenu", product.isAvailableDigitalMenu());
        node.put("availablePdv", product.isAvailablePdv());

        ArrayNode cpArray = node.putArray("channelPrices");
        if (product.getChannelPrices() != null) {
            for (ChannelPrice cp : product.getChannelPrices()) {
                ObjectNode cpNode = cpArray.addObject();
                putText(cpNode, "channel", cp.getChannel());
                putDecimal(cpNode, "price", defaultZero(cp.getPrice()));
            }
        }

        ArrayNode tsArray = node.putArray("technicalSheet");
        if (product.getTechnicalSheet() != null) {
            for (TechnicalSheetItem ts : product.getTechnicalSheet()) {
                ObjectNode tsNode = tsArray.addObject();
                putUuid(tsNode, "ingredientId", ts.getIngredientId());
                putDecimal(tsNode, "quantity", defaultZero(ts.getQuantity()));
                putDecimal(tsNode, "costSnapshot", defaultZero(ts.getCostSnapshot()));
            }
        }

        ArrayNode remArray = node.putArray("removableItemIds");
        if (product.getRemovableItemIds() != null) {
            for (UUID remId : product.getRemovableItemIds()) {
                if (remId != null) {
                    remArray.add(remId.toString());
                }
            }
        }

        ArrayNode modArray = node.putArray("modifierGroupIds");
        if (product.getModifierGroupIds() != null) {
            for (UUID modId : product.getModifierGroupIds()) {
                if (modId != null) {
                    modArray.add(modId.toString());
                }
            }
        }

        putInstant(node, "updatedAt", updatedAt != null ? updatedAt : Instant.now());
        putDateTime(node, "deletedAt", deletedAt);
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

    private void putInstant(ObjectNode node, String field, Instant value) {
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
