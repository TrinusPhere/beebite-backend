package com.fidecode.biteflow.domain.order;

import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.InvalidOrderTransitionException;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderDomainServiceTest {

    private OrderDomainService service;
    private static final UUID STORE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new OrderDomainService();
    }

    // ── advanceStatus ──

    @Test
    void deveAvancarDeAnaliseParaPreparo() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        assertEquals(OrderStatus.ANALISE, order.getStatus());

        service.advanceStatus(order);
        assertEquals(OrderStatus.PREPARO, order.getStatus());
    }

    @Test
    void deveAvancarDePreparoParaEmRotaDelivery() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.PREPARO);
        order.assignDriver(UUID.randomUUID(), "Motoboy");

        service.advanceStatus(order);
        assertEquals(OrderStatus.EM_ROTA, order.getStatus());
    }

    @Test
    void deveAvancarDeEmRotaParaFinalizado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.EM_ROTA);
        order.assignDriver(UUID.randomUUID(), "Motoboy");

        service.advanceStatus(order);
        assertEquals(OrderStatus.FINALIZADO, order.getStatus());
        assertNotNull(order.getFinishedAt());
    }

    @Test
    void balcaoDevePularEmRotaEIrDiretoParaFinalizado() {
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        order.changeStatus(OrderStatus.PREPARO);

        service.advanceStatus(order);
        assertEquals(OrderStatus.FINALIZADO, order.getStatus());
        assertNotNull(order.getFinishedAt());
    }

    @Test
    void deveLancarExcecaoAoAvancarPedidoCancelado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.CANCELADO);

        assertThrows(InvalidOrderTransitionException.class,
            () -> service.advanceStatus(order));
    }

    @Test
    void deveLancarExcecaoAoAvancarPedidoFinalizado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.FINALIZADO);

        assertThrows(InvalidOrderTransitionException.class,
            () -> service.advanceStatus(order));
    }

    @Test
    void deveLancarExcecaoAoAvancarDeliverySemMotoboyParaEmRota() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.PREPARO);
        // sem driverId

        assertThrows(InvalidOrderTransitionException.class,
            () -> service.advanceStatus(order));
    }

    // ── moveToColumn ──

    @Test
    void deveMoverPedidoParaColunaEspecifica() {
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        service.moveToColumn(order, OrderStatus.PREPARO);
        assertEquals(OrderStatus.PREPARO, order.getStatus());
    }

    @Test
    void deveLancarExcecaoAoMoverPedidoCancelado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.CANCELADO);

        assertThrows(InvalidOrderTransitionException.class,
            () -> service.moveToColumn(order, OrderStatus.PREPARO));
    }

    @Test
    void deveLancarExcecaoAoMoverDeliveryParaEmRotaSemMotoboy() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.PREPARO);

        assertThrows(InvalidOrderTransitionException.class,
            () -> service.moveToColumn(order, OrderStatus.EM_ROTA));
    }

    @Test
    void deveMoverDeliveryParaEmRotaComMotoboy() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.PREPARO);
        order.assignDriver(UUID.randomUUID(), "Carlos");

        service.moveToColumn(order, OrderStatus.EM_ROTA);
        assertEquals(OrderStatus.EM_ROTA, order.getStatus());
    }

    @Test
    void deveSetarFinishedAtAoMoverParaFinalizado() {
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        order.changeStatus(OrderStatus.PREPARO);

        service.moveToColumn(order, OrderStatus.FINALIZADO);
        assertEquals(OrderStatus.FINALIZADO, order.getStatus());
        assertNotNull(order.getFinishedAt());
    }

    // ── assignDriver ──

    @Test
    void deveAtribuirMotoboy() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        UUID driverId = UUID.randomUUID();

        service.assignDriver(order, driverId, "Carlos");
        assertEquals(driverId, order.getDriverId());
        assertEquals("Carlos", order.getDriverName());
    }
}
