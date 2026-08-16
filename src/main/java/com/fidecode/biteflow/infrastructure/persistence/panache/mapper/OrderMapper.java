package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.order.entity.*;
import com.fidecode.biteflow.domain.order.valueobject.*;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.*;

import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

    private OrderMapper() {}

    public static OrderEntity toEntity(Order order) {
        OrderEntity e = new OrderEntity();
        e.id = order.getId();
        e.storeId = order.getStoreId();
        e.displayId = order.getDisplayId();
        e.customerId = order.getCustomerId();
        e.customerName = order.getCustomerName();
        e.customerAddress = order.getCustomerAddress();
        e.type = order.getType().name();
        e.status = order.getStatus().name();
        e.paymentMethod = order.getPaymentMethod().name();
        e.driverId = order.getDriverId();
        e.driverName = order.getDriverName();
        e.platform = order.getPlatform().name();
        e.externalOrderId = order.getExternalOrderId();
        e.subtotal = order.getSubtotal();
        e.deliveryCost = order.getDeliveryCost();
        e.totalValue = order.getTotalValue();
        e.nfeEmitted = order.isNfeEmitted();
        e.createdAt = order.getCreatedAt();
        e.updatedAt = order.getUpdatedAt();
        e.finishedAt = order.getFinishedAt();
        return e;
    }

    public static Order toDomain(OrderEntity e, List<OrderItem> items) {
        Order order = new Order(e.storeId, OrderType.valueOf(e.type), PaymentMethod.valueOf(e.paymentMethod));
        order.setId(e.id);
        order.setDisplayId(e.displayId);
        order.setCustomerId(e.customerId);
        order.setCustomerName(e.customerName);
        order.setCustomerAddress(e.customerAddress);
        order.setStatus(OrderStatus.valueOf(e.status));
        order.setDriverId(e.driverId);
        order.setDriverName(e.driverName);
        order.setPlatform(OrderPlatform.valueOf(e.platform));
        order.setExternalOrderId(e.externalOrderId);
        order.setSubtotal(e.subtotal);
        order.setDeliveryCost(e.deliveryCost);
        order.setTotalValue(e.totalValue);
        order.setNfeEmitted(e.nfeEmitted);
        order.setItems(items != null ? items : new ArrayList<>());
        order.setCreatedAt(e.createdAt);
        order.setUpdatedAt(e.updatedAt);
        order.setFinishedAt(e.finishedAt);
        return order;
    }

    public static OrderItemEntity toItemEntity(OrderItem item, java.util.UUID orderId) {
        OrderItemEntity e = new OrderItemEntity();
        e.id = item.getId();
        e.orderId = orderId;
        e.productId = item.getProductId();
        e.productName = item.getProductName();
        e.quantity = item.getQuantity();
        e.unitPrice = item.getUnitPrice();
        e.extraPrice = item.getExtraPrice();
        e.totalPrice = item.getTotalPrice();
        e.observation = item.getObservation();
        return e;
    }

    public static OrderItem toItemDomain(OrderItemEntity e,
                                          List<OrderItemModifier> modifiers,
                                          List<OrderItemRemoval> removals) {
        OrderItem item = new OrderItem(e.productId, e.productName, e.quantity, e.unitPrice, e.observation);
        item.setId(e.id);
        item.setExtraPrice(e.extraPrice);
        item.setTotalPrice(e.totalPrice);
        item.setModifiers(modifiers != null ? modifiers : new ArrayList<>());
        item.setRemovals(removals != null ? removals : new ArrayList<>());
        return item;
    }

    public static OrderItemModifierEntity toModifierEntity(OrderItemModifier mod, java.util.UUID orderItemId) {
        OrderItemModifierEntity e = new OrderItemModifierEntity();
        e.id = mod.getId();
        e.orderItemId = orderItemId;
        e.modifierGroupId = mod.getGroupId();
        e.groupName = mod.getGroupName();
        e.modifierOptionId = mod.getOptionId();
        e.optionName = mod.getOptionName();
        e.price = mod.getPrice();
        return e;
    }

    public static OrderItemModifier toModifierDomain(OrderItemModifierEntity e) {
        OrderItemModifier mod = new OrderItemModifier(
            e.modifierGroupId, e.groupName, e.modifierOptionId, e.optionName, e.price);
        mod.setId(e.id);
        return mod;
    }

    public static OrderItemRemovalEntity toRemovalEntity(OrderItemRemoval rem, java.util.UUID orderItemId) {
        OrderItemRemovalEntity e = new OrderItemRemovalEntity();
        e.id = rem.getId();
        e.orderItemId = orderItemId;
        e.ingredientId = rem.getIngredientId();
        e.ingredientName = rem.getIngredientName();
        return e;
    }

    public static OrderItemRemoval toRemovalDomain(OrderItemRemovalEntity e) {
        OrderItemRemoval rem = new OrderItemRemoval(e.ingredientId, e.ingredientName);
        rem.setId(e.id);
        return rem;
    }
}
