package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.OrderResponse;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.entity.OrderItem;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class OrderDtoMapper {
    private OrderDtoMapper() {}
    public static OrderResponse toResponse(Order o) {
        List<OrderResponse.OrderItemResponse> items = o.getItems()!=null ? o.getItems().stream().map(OrderDtoMapper::toItemResponse).toList() : List.of();
        return new OrderResponse(
            o.getId()!=null?o.getId().toString():null, o.getDisplayId(), o.getCustomerId()!=null?o.getCustomerId().toString():null,
            o.getCustomerName(), o.getCustomerAddress(), o.getCustomerPhone(), o.getCustomerAddressComplement(), o.getCustomerAddressReference(),
            o.getType().name(), o.getOperation(), o.getChannel(), o.getServiceType(), o.getSource(), o.getStatus().name(), o.getPaymentMethod().name(),
            o.getCashChangeFor(), o.getNotes(), o.getDriverId()!=null?o.getDriverId().toString():null, o.getDriverName(), o.getPlatform().name(), o.getExternalOrderId(),
            o.getSubtotal(), o.getDeliveryCost(), o.getTotalValue(), o.isNfeEmitted(), items,
            o.getCreatedAt()!=null?o.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME):null,
            o.getFinishedAt()!=null?o.getFinishedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME):null);
    }
    public static OrderResponse.OrderItemResponse toItemResponse(OrderItem item){
        var mods=item.getModifiers()!=null?item.getModifiers().stream().map(m->new OrderResponse.ModifierResponse(m.getId()!=null?m.getId().toString():null,m.getGroupId()!=null?m.getGroupId().toString():null,m.getGroupName(),m.getOptionId()!=null?m.getOptionId().toString():null,m.getOptionName(),m.getPrice())).toList():List.<OrderResponse.ModifierResponse>of();
        var rems=item.getRemovals()!=null?item.getRemovals().stream().map(r->new OrderResponse.RemovalResponse(r.getId()!=null?r.getId().toString():null,r.getIngredientId()!=null?r.getIngredientId().toString():null,r.getIngredientName())).toList():List.<OrderResponse.RemovalResponse>of();
        return new OrderResponse.OrderItemResponse(item.getId()!=null?item.getId().toString():null,item.getProductId()!=null?item.getProductId().toString():null,item.getProductName(),item.getQuantity(),item.getUnitPrice(),item.getExtraPrice(),item.getTotalPrice(),item.getObservation(),mods,rems);
    }
}
