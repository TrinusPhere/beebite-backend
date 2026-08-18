package com.fidecode.biteflow.domain.order;

import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.entity.OrderItem;
import com.fidecode.biteflow.domain.order.valueobject.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void deveCriarPedidoComValoresPadrao() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);

        assertEquals(STORE_ID, order.getStoreId());
        assertEquals(OrderType.DELIVERY, order.getType());
        assertEquals(PaymentMethod.PIX, order.getPaymentMethod());
        assertEquals(OrderStatus.ANALISE, order.getStatus());
        assertEquals(OrderPlatform.OWN, order.getPlatform());
        assertFalse(order.isNfeEmitted());
        assertEquals(BigDecimal.ZERO, order.getSubtotal());
        assertEquals(BigDecimal.ZERO, order.getTotalValue());
        assertNotNull(order.getItems());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void deveLancarExcecaoSeStoreIdNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(null, OrderType.BALCAO, PaymentMethod.DINHEIRO));
    }

    @Test
    void deveLancarExcecaoSeTipoNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(STORE_ID, null, PaymentMethod.DINHEIRO));
    }

    @Test
    void deveLancarExcecaoSePaymentMethodNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(STORE_ID, OrderType.BALCAO, null));
    }

    @Test
    void deveCalcularTotalValueComItens() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);

        OrderItem item1 = new OrderItem(UUID.randomUUID(), "X-Bacon", 2,
            new BigDecimal("25.00"), null);
        OrderItem item2 = new OrderItem(UUID.randomUUID(), "Suco", 1,
            new BigDecimal("8.00"), null);

        order.addItem(item1);
        order.addItem(item2);

        // subtotal = (25*2) + (8*1) = 58
        assertEquals(new BigDecimal("58.00"), order.getSubtotal());
        assertEquals(new BigDecimal("58.00"), order.getTotalValue());
    }

    @Test
    void deveCalcularTotalValueComDeliveryCost() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setDeliveryCost(new BigDecimal("10.00"));

        OrderItem item = new OrderItem(UUID.randomUUID(), "X-Bacon", 1,
            new BigDecimal("25.00"), null);
        order.addItem(item);

        // totalValue = 25 + 10 = 35
        assertEquals(new BigDecimal("25.00"), order.getSubtotal());
        assertEquals(new BigDecimal("35.00"), order.getTotalValue());
    }

    @Test
    void deveMarcarNfeComoEmitida() {
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        assertFalse(order.isNfeEmitted());

        order.markAsNfeEmitted();
        assertTrue(order.isNfeEmitted());
    }

    @Test
    void deveSetarDadosDoCliente() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.updateCustomerInfo("João Silva", "Rua das Flores, 123");

        assertEquals("João Silva", order.getCustomerName());
        assertEquals("Rua das Flores, 123", order.getCustomerAddress());
    }
}
