package com.fidecode.beebite.application.printing;

import com.fidecode.beebite.domain.order.entity.Order;
import com.fidecode.beebite.domain.order.entity.OrderItem;
import com.fidecode.beebite.domain.order.entity.OrderItemModifier;
import com.fidecode.beebite.domain.order.entity.OrderItemRemoval;
import com.fidecode.beebite.domain.order.valueobject.OrderType;
import com.fidecode.beebite.domain.order.valueobject.PaymentMethod;
import java.math.BigDecimal;
import java.util.UUID;

/** Pedido fictício usado apenas na pré-visualização do modelo de comanda. Nunca é persistido. */
final class SampleOrderFactory {
    private SampleOrderFactory() {
    }

    static Order create(UUID storeId) {
        var order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.DINHEIRO);
        order.setDisplayId("1234");
        order.defineContext("DELIVERY", "CARDAPIO_PROPRIO", "ENTREGA", "WEB");
        order.updateCustomerInfo("Maria Silva", "Rua das Flores, 123 - Centro");
        order.updateCustomerContact("(11) 99999-8888", "Apto 12", "Próximo à praça");
        order.changeDeliveryCost(new BigDecimal("6.00"));
        order.defineCashChangeFor(new BigDecimal("100.00"));
        order.updateNotes("Tocar a campainha");

        var burger = new OrderItem(UUID.randomUUID(), "X-Bacon Mestre", 2, new BigDecimal("29.90"), "Bem passado");
        burger.addModifier(new OrderItemModifier(UUID.randomUUID(), "Adicionais", UUID.randomUUID(), "Queijo extra",
                new BigDecimal("3.00")));
        burger.addRemoval(new OrderItemRemoval(UUID.randomUUID(), "Cebola"));
        order.addItem(burger);
        order.addItem(new OrderItem(UUID.randomUUID(), "Coca-Cola Lata 350ml", 1, new BigDecimal("8.00"), null));
        return order;
    }
}

