package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.order.entity.*;
import com.trinusphere.beebite.domain.order.valueobject.*;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {
    private OrderMapper() {
    }

    public static OrderEntity toEntity(Order order) {
        OrderEntity e = new OrderEntity();
        e.id = order.getId();
        e.version = order.getVersion();
        e.storeId = order.getStoreId();
        e.displayId = order.getDisplayId();
        e.customerId = order.getCustomerId();
        e.customerName = order.getCustomerName();
        e.customerAddress = order.getCustomerAddress();
        e.customerPhone = order.getCustomerPhone();
        e.customerAddressComplement = order.getCustomerAddressComplement();
        e.customerAddressReference = order.getCustomerAddressReference();
        e.type = order.getType().name();
        e.status = order.getStatus().name();
        e.paymentMethod = order.getPaymentMethod().name();
        e.driverId = order.getDriverId();
        e.driverName = order.getDriverName();
        e.platform = order.getPlatform().name();
        e.externalOrderId = order.getExternalOrderId();
        e.operation = order.getOperation();
        e.channel = order.getChannel();
        e.serviceType = order.getServiceType();
        e.source = order.getSource();
        e.cashChangeFor = order.getCashChangeFor();
        e.notes = order.getNotes();
        e.subtotal = order.getSubtotal();
        e.deliveryCost = order.getDeliveryCost();
        e.totalValue = order.getTotalValue();
        e.nfeEmitted = order.isNfeEmitted();
        e.createdAt = order.getCreatedAt();
        e.updatedAt = order.getUpdatedAt();
        e.finishedAt = order.getFinishedAt();
        if (order.getItems() != null)
            for (OrderItem item : order.getItems()) {
                OrderItemEntity ie = toItemEntity(item);
                ie.order = e;
                e.items.add(ie);
            }
        return e;
    }

    public static Order toDomain(OrderEntity e, List<OrderItem> items) {
        return Order.reconstitute(e.id, e.storeId, e.displayId, e.version, e.customerId, e.customerName,
                e.customerAddress, e.customerPhone, e.customerAddressComplement, e.customerAddressReference,
                OrderType.valueOf(e.type), OrderStatus.valueOf(e.status), PaymentMethod.valueOf(e.paymentMethod),
                e.driverId, e.driverName, OrderPlatform.valueOf(e.platform), e.externalOrderId, e.operation, e.channel,
                e.serviceType, e.source, e.cashChangeFor, e.notes, e.subtotal, e.deliveryCost, e.totalValue,
                e.nfeEmitted, items != null ? items : new ArrayList<>(), e.createdAt, e.updatedAt, e.finishedAt);
    }

    public static OrderItemEntity toItemEntity(OrderItem item) {
        OrderItemEntity e = new OrderItemEntity();
        e.id = item.getId();
        e.productId = item.getProductId();
        e.productName = item.getProductName();
        e.quantity = item.getQuantity();
        e.unitPrice = item.getUnitPrice();
        e.extraPrice = item.getExtraPrice();
        e.totalPrice = item.getTotalPrice();
        e.observation = item.getObservation();
        if (item.getModifiers() != null)
            for (OrderItemModifier mod : item.getModifiers()) {
                var me = toModifierEntity(mod);
                me.orderItem = e;
                e.modifiers.add(me);
            }
        if (item.getRemovals() != null)
            for (OrderItemRemoval rem : item.getRemovals()) {
                var re = toRemovalEntity(rem);
                re.orderItem = e;
                e.removals.add(re);
            }
        return e;
    }

    public static OrderItem toItemDomain(OrderItemEntity e, List<OrderItemModifier> modifiers,
            List<OrderItemRemoval> removals) {
        OrderItem item = new OrderItem(e.productId, e.productName, e.quantity, e.unitPrice, e.observation);
        item.setId(e.id);
        item.setExtraPrice(e.extraPrice);
        item.setTotalPrice(e.totalPrice);
        item.setModifiers(modifiers != null ? modifiers : new ArrayList<>());
        item.setRemovals(removals != null ? removals : new ArrayList<>());
        return item;
    }

    public static OrderItemModifierEntity toModifierEntity(OrderItemModifier mod) {
        OrderItemModifierEntity e = new OrderItemModifierEntity();
        e.id = mod.getId();
        e.modifierGroupId = mod.getGroupId();
        e.groupName = mod.getGroupName();
        e.modifierOptionId = mod.getOptionId();
        e.optionName = mod.getOptionName();
        e.price = mod.getPrice();
        return e;
    }

    public static OrderItemModifier toModifierDomain(OrderItemModifierEntity e) {
        OrderItemModifier m = new OrderItemModifier(e.modifierGroupId, e.groupName, e.modifierOptionId, e.optionName,
                e.price);
        m.setId(e.id);
        return m;
    }

    public static OrderItemRemovalEntity toRemovalEntity(OrderItemRemoval rem) {
        OrderItemRemovalEntity e = new OrderItemRemovalEntity();
        e.id = rem.getId();
        e.ingredientId = rem.getIngredientId();
        e.ingredientName = rem.getIngredientName();
        return e;
    }

    public static OrderItemRemoval toRemovalDomain(OrderItemRemovalEntity e) {
        OrderItemRemoval r = new OrderItemRemoval(e.ingredientId, e.ingredientName);
        r.setId(e.id);
        return r;
    }
}
