package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.domain.financial.event.OrderFinalizedEvent;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderFinalizationPublisher {
    private final Event<OrderFinalizedEvent> event;

    @Inject
    public OrderFinalizationPublisher(Event<OrderFinalizedEvent> event) {
        this.event = event;
    }

    public void publishIfFinalized(Order order) {
        if (order.getStatus() != OrderStatus.FINALIZADO)
            return;
        var products = order.getItems().stream().map(i -> new OrderFinalizedEvent.ProductSnapshot(i.getProductId(),
                i.getProductName(), i.getQuantity(), i.getUnitPrice())).toList();
        event.fire(new OrderFinalizedEvent(order.getId(), order.getStoreId(), order.getTotalValue(),
                order.getPaymentMethod(), order.getCustomerId(), order.getDriverId(), products));
    }
}
