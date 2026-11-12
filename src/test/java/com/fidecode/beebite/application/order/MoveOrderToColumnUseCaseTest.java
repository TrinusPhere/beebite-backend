package com.fidecode.beebite.application.order;

import com.fidecode.beebite.application.kds.CancelProductionTasksByOrderUseCase;
import com.fidecode.beebite.application.kds.DispatchOrderToKdsUseCase;
import com.fidecode.beebite.application.shared.port.TransactionExecutor;
import com.fidecode.beebite.domain.kds.entity.KdsSettings;
import com.fidecode.beebite.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.beebite.domain.order.entity.Order;
import com.fidecode.beebite.domain.order.repository.OrderRepository;
import com.fidecode.beebite.domain.order.service.OrderDomainService;
import com.fidecode.beebite.domain.order.valueobject.OrderStatus;
import com.fidecode.beebite.domain.order.valueobject.OrderType;
import com.fidecode.beebite.domain.order.valueobject.PaymentMethod;
import com.fidecode.beebite.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoveOrderToColumnUseCaseTest {

    @Mock
    private OrderRepository repo;
    @Mock
    private OrderDomainService service;
    @Mock
    private OrderFinalizationPublisher publisher;
    @Mock
    private CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase;
    @Mock
    private DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase;
    @Mock
    private KdsSettingsRepository kdsSettingsRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private com.fidecode.beebite.application.order.port.BiteflowEventPublisher eventPublisher;

    private MoveOrderToColumnUseCase useCase;

    private final UUID storeId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TransactionExecutor tx = Supplier::get;
        useCase = new MoveOrderToColumnUseCase(
                repo, service, publisher, tx,
                cancelProductionTasksByOrderUseCase, dispatchOrderToKdsUseCase,
                kdsSettingsRepository, paymentRepository, eventPublisher
        );
    }

    @Test
    void devePublicarEventoQuandoMoverPedidoParaOutraColuna() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setId(orderId);
        order.setPlatform(com.fidecode.beebite.domain.order.valueobject.OrderPlatform.IFOOD);
        order.setExternalOrderId("ifood-test-123");

        when(repo.findById(orderId)).thenReturn(Optional.of(order));
        when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(storeId, orderId, "PREPARO");

        verify(eventPublisher).publishStatusChanged(any(Order.class));
    }

    @Test
    void deveCancelarTarefasKdsAoMoverParaEmRota() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setId(orderId);
        order.changeStatus(OrderStatus.PREPARO);

        when(repo.findById(orderId)).thenReturn(Optional.of(order));
        doAnswer(inv -> {
            order.changeStatus(OrderStatus.EM_ROTA);
            return null;
        }).when(service).moveToColumn(order, OrderStatus.EM_ROTA);
        when(repo.save(order)).thenReturn(order);

        useCase.execute(storeId, orderId, "EM_ROTA");

        verify(cancelProductionTasksByOrderUseCase).execute(storeId, orderId);
        verify(dispatchOrderToKdsUseCase, never()).execute(any(), any());
    }

    @Test
    void deveCancelarTarefasKdsAoMoverParaFinalizado() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setId(orderId);
        order.changeStatus(OrderStatus.PREPARO);

        when(repo.findById(orderId)).thenReturn(Optional.of(order));
        doAnswer(inv -> {
            order.changeStatus(OrderStatus.FINALIZADO);
            return null;
        }).when(service).moveToColumn(order, OrderStatus.FINALIZADO);
        when(repo.save(order)).thenReturn(order);

        useCase.execute(storeId, orderId, "FINALIZADO");

        verify(cancelProductionTasksByOrderUseCase).execute(storeId, orderId);
        verify(dispatchOrderToKdsUseCase, never()).execute(any(), any());
    }

    @Test
    void deveDespacharParaKdsAoMoverParaPreparo() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setId(orderId);

        when(repo.findById(orderId)).thenReturn(Optional.of(order));
        doAnswer(inv -> {
            order.changeStatus(OrderStatus.PREPARO);
            return null;
        }).when(service).moveToColumn(order, OrderStatus.PREPARO);
        when(repo.save(order)).thenReturn(order);
        when(kdsSettingsRepository.findByStoreId(storeId)).thenReturn(Optional.of(KdsSettings.createDefault(storeId)));

        useCase.execute(storeId, orderId, "PREPARO");

        verify(dispatchOrderToKdsUseCase).execute(storeId, orderId);
        verify(cancelProductionTasksByOrderUseCase, never()).execute(any(), any());
    }
}
