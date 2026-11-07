package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.*;
import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.repository.CashSessionRepository;
import com.fidecode.biteflow.domain.cash.valueobject.CashSessionStatus;
import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;
import com.fidecode.biteflow.domain.catalog.entity.ModifierOption;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ModifierGroupRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.entity.OrderItem;
import com.fidecode.biteflow.domain.order.exception.InvalidOrderTransitionException;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.*;
import com.fidecode.biteflow.domain.financial.event.OrderFinalizedEvent;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUseCasesTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ModifierGroupRepository modifierGroupRepository;
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private CashSessionRepository cashSessionRepository;
    @Mock
    private com.fidecode.biteflow.application.order.port.BiteflowEventPublisher biteflowEventPublisher;

    private CreateOrderUseCase createOrderUseCase;
    private ManageOrderUseCase manageOrderUseCase;
    private UpdateOrderUseCase updateOrderUseCase;
    private GetOrderKanbanUseCase getOrderKanbanUseCase;
    private DeleteOrderUseCase deleteOrderUseCase;
    @Mock
    private com.fidecode.biteflow.application.kds.CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase;

    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private Event<OrderFinalizedEvent> orderFinalizedEvent;

    private final UUID STORE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createOrderUseCase = new CreateOrderUseCase(orderRepository, productRepository, modifierGroupRepository,
                ingredientRepository);
        manageOrderUseCase = new ManageOrderUseCase(orderRepository, orderDomainService, orderFinalizedEvent,
                biteflowEventPublisher);

        updateOrderUseCase = new UpdateOrderUseCase(orderRepository, productRepository, modifierGroupRepository,
                ingredientRepository);
        getOrderKanbanUseCase = new GetOrderKanbanUseCase(orderRepository);
        deleteOrderUseCase = new DeleteOrderUseCase(orderRepository, cancelProductionTasksByOrderUseCase);
    }

    @Test
    void deveCriarPedidoComItens() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(STORE_ID, "X-Bacon", UUID.randomUUID(), new BigDecimal("25.00"));
        product.setId(productId);

        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of(product));
        when(ingredientRepository.findAllById(any())).thenReturn(List.of());
        when(modifierGroupRepository.findAllById(any())).thenReturn(List.of());
        when(orderRepository.getNextDisplayIdSequence()).thenReturn(1001L);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        CreateOrderRequest request = new CreateOrderRequest("DELIVERY",
                new CreateOrderRequest.CustomerInfo(null, "João", "Rua 1"), "PIX",
                List.of(new CreateOrderItemDto(productId.toString(), 2, "Capricha", null, null)),
                new BigDecimal("5.00"));

        OrderResponse response = createOrderUseCase.create(STORE_ID, request);

        assertNotNull(response.id());
        assertEquals("1001", response.displayId());
        assertEquals("João", response.customerName());
        assertEquals("DELIVERY", response.type());
        assertEquals("ANALISE", response.status());
        assertEquals(1, response.items().size());
        assertEquals("X-Bacon", response.items().get(0).productName());
        assertEquals(2, response.items().get(0).quantity());
    }

    @Test
    void deveAvancarStatusDoPedido() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        order.setId(orderId);
        order.addItem(new OrderItem(UUID.randomUUID(), "X-Bacon", 1, new BigDecimal("25.00"), null));

        // Mock deve delegar ao domain service real para mutar o status
        doAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.changeStatus(OrderStatus.PREPARO);
            return null;
        }).when(orderDomainService).advanceStatus(any(Order.class));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = manageOrderUseCase.advanceStatus(STORE_ID, orderId);
        assertEquals("PREPARO", response.status());
    }

    @Test
    void deveLancarExcecaoAoAvancarPedidoInexistente() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> manageOrderUseCase.advanceStatus(STORE_ID, orderId));
    }

    @Test
    void deveMoverPedidoParaColuna() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        order.setId(orderId);

        doAnswer(inv -> {
            Order o = inv.getArgument(0);
            OrderStatus target = inv.getArgument(1);
            o.changeStatus(target);
            return null;
        }).when(orderDomainService).moveToColumn(any(Order.class), any(OrderStatus.class));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = manageOrderUseCase.moveToColumn(STORE_ID, orderId, "PREPARO");
        assertEquals("PREPARO", response.status());
    }

    @Test
    void deveAtribuirMotoboy() {
        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setId(orderId);

        doAnswer(inv -> {
            Order o = inv.getArgument(0);
            UUID did = inv.getArgument(1);
            String dname = inv.getArgument(2);
            o.assignDriver(did, dname);
            return null;
        }).when(orderDomainService).assignDriver(any(Order.class), any(UUID.class), anyString());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = manageOrderUseCase.assignDriver(STORE_ID, orderId,
                new AssignDriverRequest(driverId.toString(), "Carlos", null));
        assertEquals(driverId.toString(), response.driverId());
        assertEquals("Carlos", response.driverName());
    }

    @Test
    void deveListarKanbanComQuatroColunas() {
        Order o1 = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        o1.setId(UUID.randomUUID());
        o1.setDisplayId("#1001");
        o1.addItem(new OrderItem(UUID.randomUUID(), "X-Bacon", 1, new BigDecimal("25.00"), null));

        Order o2 = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        o2.setId(UUID.randomUUID());
        o2.setDisplayId("#1002");
        o2.changeStatus(OrderStatus.PREPARO);
        o2.addItem(new OrderItem(UUID.randomUUID(), "Suco", 1, new BigDecimal("8.00"), null));

        when(orderRepository.findActiveKanbanOrdersByStoreId(eq(STORE_ID), any())).thenReturn(List.of(o1, o2));

        OrderKanbanResponse kanban = getOrderKanbanUseCase.listKanban(STORE_ID, null);

        assertEquals(4, kanban.columns().size());
        assertEquals("ANALISE", kanban.columns().get(0).id());
        assertEquals(1, kanban.columns().get(0).orders().size());
        assertEquals("PREPARO", kanban.columns().get(1).id());
        assertEquals(1, kanban.columns().get(1).orders().size());
        assertEquals("EM_ROTA", kanban.columns().get(2).id());
        assertEquals(0, kanban.columns().get(2).orders().size());
    }

    @Test
    void deveEditarPedidoAdicionandoItens() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Order order = new Order(STORE_ID, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setId(orderId);
        order.addItem(new OrderItem(UUID.randomUUID(), "X-Bacon", 1, new BigDecimal("25.00"), null));

        Product product = new Product(STORE_ID, "Batata Frita", UUID.randomUUID(), new BigDecimal("12.00"));
        product.setId(productId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of(product));
        when(ingredientRepository.findAllById(any())).thenReturn(List.of());
        when(modifierGroupRepository.findAllById(any())).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateOrderRequest request = new UpdateOrderRequest(null, null, null,
                List.of(new CreateOrderItemDto(productId.toString(), 1, "", null, null)));

        OrderResponse response = updateOrderUseCase.update(STORE_ID, orderId, request);

        assertEquals(2, response.items().size());
    }

    @Test
    void deveEmitirNfeSemErros() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        NfeResponse nfe = manageOrderUseCase.emitNfe(STORE_ID, orderId);

        assertEquals(orderId.toString(), nfe.orderId());
        assertEquals("AUTORIZADA", nfe.status());
        assertNotNull(nfe.nfeNumber());
    }

    @Test
    void deveDeletarPedido() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.DINHEIRO);
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        deleteOrderUseCase.delete(STORE_ID, orderId);
        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void deveLancarExcecaoAoDeletarPedidoInexistente() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> deleteOrderUseCase.delete(STORE_ID, orderId));
    }

    @Test
    void deveLancarExcecaoAoEditarPedidoEmRota() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.reconstitute(orderId, STORE_ID, "#9999", null, "João", null, OrderType.DELIVERY,
                OrderStatus.EM_ROTA, PaymentMethod.PIX, null, null, OrderPlatform.OWN, null, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, List.of(), java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(), null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateOrderRequest request = new UpdateOrderRequest(null, null, null, null);

        assertThrows(InvalidOrderTransitionException.class,
                () -> updateOrderUseCase.update(STORE_ID, orderId, request));
    }

    @Test
    void deveLancarExcecaoAoEditarPedidoFinalizado() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.reconstitute(orderId, STORE_ID, "#9998", null, "Maria", null, OrderType.BALCAO,
                OrderStatus.FINALIZADO, PaymentMethod.DINHEIRO, null, null, OrderPlatform.OWN, null, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, List.of(), java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(), null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateOrderRequest request = new UpdateOrderRequest(null, null, null, null);

        assertThrows(InvalidOrderTransitionException.class,
                () -> updateOrderUseCase.update(STORE_ID, orderId, request));
    }

    @Test
    void kanbanDeveIncluirPedidosFinalizadosECanceladosNaColunaFinalizado() {
        Order finalizado = Order.reconstitute(UUID.randomUUID(), STORE_ID, "#1001", null, "Cliente 1", null,
                OrderType.DELIVERY, OrderStatus.FINALIZADO, PaymentMethod.PIX, null, null, OrderPlatform.OWN,
                null, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, false, List.of(),
                java.time.LocalDateTime.now().minusMinutes(10), java.time.LocalDateTime.now(), null);

        Order cancelado = Order.reconstitute(UUID.randomUUID(), STORE_ID, "#1002", null, "Cliente 2", null,
                OrderType.BALCAO, OrderStatus.CANCELADO, PaymentMethod.DINHEIRO, null, null, OrderPlatform.OWN,
                null, BigDecimal.valueOf(25), BigDecimal.ZERO, BigDecimal.ZERO, false, List.of(),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), null);

        when(orderRepository.findActiveKanbanOrdersByStoreId(eq(STORE_ID), any()))
                .thenReturn(List.of(finalizado, cancelado));

        var kanban = getOrderKanbanUseCase.listKanban(STORE_ID, null);

        assertNotNull(kanban);
        var finalizadoCol = kanban.columns().stream()
                .filter(c -> "FINALIZADO".equals(c.id()))
                .findFirst().orElseThrow();

        assertEquals(2, finalizadoCol.orders().size());
        assertTrue(finalizadoCol.orders().stream().anyMatch(o -> "#1001".equals(o.displayId()) && "FINALIZADO".equals(o.status())));
        assertTrue(finalizadoCol.orders().stream().anyMatch(o -> "#1002".equals(o.displayId()) && "CANCELADO".equals(o.status())));
    }

    @Test
    void kanbanDeveEstarLimpoQuandoCaixaEstiverEncerrado() {
        GetOrderKanbanUseCase useCase = new GetOrderKanbanUseCase(orderRepository, cashSessionRepository);

        when(cashSessionRepository.findByStoreIdAndStatus(STORE_ID, CashSessionStatus.OPEN))
                .thenReturn(List.of());

        var kanban = useCase.listKanban(STORE_ID, null);

        assertNotNull(kanban);
        assertEquals(4, kanban.columns().size());
        assertTrue(kanban.columns().stream().allMatch(col -> col.orders().isEmpty()));
        verify(orderRepository, never()).findActiveKanbanOrdersByStoreId(any(), any());
    }

    @Test
    void kanbanDeveFiltrarSomentePedidosAposAberturaDoCaixaEspecifico() {
        GetOrderKanbanUseCase useCase = new GetOrderKanbanUseCase(orderRepository, cashSessionRepository);

        var sessionOpenedAt = java.time.LocalDateTime.now().minusHours(2);
        CashSession session = new CashSession(STORE_ID, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        session = CashSession.reconstitute(UUID.randomUUID(), STORE_ID, UUID.randomUUID(), UUID.randomUUID(),
                CashSessionStatus.OPEN, sessionOpenedAt, null, BigDecimal.ZERO, null, null, null, null,
                sessionOpenedAt, sessionOpenedAt);

        when(cashSessionRepository.findByStoreIdAndStatus(STORE_ID, CashSessionStatus.OPEN))
                .thenReturn(List.of(session));

        Order order = Order.reconstitute(UUID.randomUUID(), STORE_ID, "#1001", null, "Cliente", null,
                OrderType.BALCAO, OrderStatus.PREPARO, PaymentMethod.DINHEIRO, null, null, OrderPlatform.OWN,
                null, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, false, List.of(),
                sessionOpenedAt.plusMinutes(10), sessionOpenedAt.plusMinutes(10), null);

        when(orderRepository.findActiveKanbanOrdersByStoreId(eq(STORE_ID), eq(sessionOpenedAt)))
                .thenReturn(List.of(order));

        var kanban = useCase.listKanban(STORE_ID, null);

        assertNotNull(kanban);
        var preparoCol = kanban.columns().stream().filter(c -> "PREPARO".equals(c.id())).findFirst().orElseThrow();
        assertEquals(1, preparoCol.orders().size());
        assertEquals("#1001", preparoCol.orders().get(0).displayId());
    }
}
