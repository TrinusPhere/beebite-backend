package com.fidecode.beebite.domain.order;

import com.fidecode.beebite.domain.order.entity.Order;
import com.fidecode.beebite.domain.order.exception.InvalidOrderTransitionException;
import com.fidecode.beebite.domain.order.service.OrderDomainService;
import com.fidecode.beebite.domain.order.valueobject.*;
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
    void deveAvancarDeEmRotaParaEntregue() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.EM_ROTA);
        order.assignDriver(UUID.randomUUID(), "Motoboy");

        service.advanceStatus(order);
        assertEquals(OrderStatus.ENTREGUE, order.getStatus());
        assertNull(order.getFinishedAt());
    }

    @Test
    void deveAvancarDeEntregueParaFinalizado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.ENTREGUE);
        order.assignDriver(UUID.randomUUID(), "Motoboy");

        service.advanceStatus(order);
        assertEquals(OrderStatus.FINALIZADO, order.getStatus());
        assertNotNull(order.getFinishedAt());
    }

    @Test
    void balcaoDeveAvancarDePreparoParaEntregueEDepoisFinalizado() {
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        order.changeStatus(OrderStatus.PREPARO);

        service.advanceStatus(order);
        assertEquals(OrderStatus.ENTREGUE, order.getStatus());

        service.advanceStatus(order);
        assertEquals(OrderStatus.FINALIZADO, order.getStatus());
        assertNotNull(order.getFinishedAt());
    }

    @Test
    void retiradaDevePularEmRotaEIrParaEntregueEDepoisFinalizado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.defineContext("DELIVERY", "CARDAPIO_PROPRIO", "RETIRADA", "WEB");
        order.changeStatus(OrderStatus.PREPARO);

        service.advanceStatus(order);
        assertEquals(OrderStatus.ENTREGUE, order.getStatus());

        service.advanceStatus(order);
        assertEquals(OrderStatus.FINALIZADO, order.getStatus());
        assertNotNull(order.getFinishedAt());
    }

    @Test
    void salaoDevePularEmRotaEIrParaEntregueEDepoisFinalizado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.defineContext("SALAO", "PDV", "MESA", "PDV_CAIXA");
        order.changeStatus(OrderStatus.PREPARO);

        service.advanceStatus(order);
        assertEquals(OrderStatus.ENTREGUE, order.getStatus());

        service.advanceStatus(order);
        assertEquals(OrderStatus.FINALIZADO, order.getStatus());
        assertNotNull(order.getFinishedAt());
    }

    @Test
    void isDeliveryWithDriverDeveDiferenciarEntregaDeRetiradaESalao() {
        Order delivery = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        delivery.defineContext("DELIVERY", "CARDAPIO_PROPRIO", "ENTREGA", "WEB");
        assertTrue(OrderDomainService.isDeliveryWithDriver(delivery));

        Order retirada = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        retirada.defineContext("DELIVERY", "CARDAPIO_PROPRIO", "RETIRADA", "WEB");
        assertFalse(OrderDomainService.isDeliveryWithDriver(retirada));

        Order salao = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        salao.defineContext("SALAO", "PDV", "CONSUMO_LOCAL", "PDV_CAIXA");
        assertFalse(OrderDomainService.isDeliveryWithDriver(salao));
    }

    @Test
    void deveLancarExcecaoAoAvancarPedidoCancelado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.CANCELADO);

        assertThrows(InvalidOrderTransitionException.class, () -> service.advanceStatus(order));
    }

    @Test
    void deveLancarExcecaoAoAvancarPedidoFinalizado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.FINALIZADO);

        assertThrows(InvalidOrderTransitionException.class, () -> service.advanceStatus(order));
    }

    @Test
    void deveLancarExcecaoAoAvancarDeliverySemMotoboyParaEmRota() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.PREPARO);
        // sem driverId

        assertThrows(InvalidOrderTransitionException.class, () -> service.advanceStatus(order));
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

        assertThrows(InvalidOrderTransitionException.class, () -> service.moveToColumn(order, OrderStatus.PREPARO));
    }

    @Test
    void deveLancarExcecaoAoMoverDeliveryParaEmRotaSemMotoboy() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.PREPARO);

        assertThrows(InvalidOrderTransitionException.class, () -> service.moveToColumn(order, OrderStatus.EM_ROTA));
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
    void naoDeveCancelarPedidoQueJaSaiuParaEntrega() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.EM_ROTA);

        assertThrows(InvalidOrderTransitionException.class, () -> service.moveToColumn(order, OrderStatus.CANCELADO));
    }

    @Test
    void naoDeveCancelarPedidoEntregue() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.ENTREGUE);

        assertThrows(InvalidOrderTransitionException.class, () -> service.moveToColumn(order, OrderStatus.CANCELADO));
    }

    @Test
    void naoDeveCancelarPedidoFinalizado() {
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.changeStatus(OrderStatus.FINALIZADO);

        assertThrows(InvalidOrderTransitionException.class, () -> service.moveToColumn(order, OrderStatus.CANCELADO));
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
