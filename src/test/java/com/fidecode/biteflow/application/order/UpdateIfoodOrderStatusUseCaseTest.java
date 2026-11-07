package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.kds.CancelProductionTasksByOrderUseCase;
import com.fidecode.biteflow.application.kds.DispatchOrderToKdsUseCase;
import com.fidecode.biteflow.application.shared.port.TransactionExecutor;
import com.fidecode.biteflow.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.order.valueobject.OrderType;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateIfoodOrderStatusUseCaseTest {

    @Mock
    private OrderRepository repo;
    @Mock
    private OrderFinalizationPublisher publisher;
    @Mock
    private DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase;
    @Mock
    private CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase;
    @Mock
    private KdsSettingsRepository kdsSettingsRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private UpdateIfoodOrderStatusUseCase useCase;

    private final UUID storeId = UUID.randomUUID();
    private final String ifoodOrderId = "ifood-123";

    @BeforeEach
    void setUp() {
        TransactionExecutor tx = Supplier::get;
        useCase = new UpdateIfoodOrderStatusUseCase(
                repo, publisher, tx,
                dispatchOrderToKdsUseCase, cancelProductionTasksByOrderUseCase,
                kdsSettingsRepository, paymentRepository
        );
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        when(repo.findByExternalOrderId(ifoodOrderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
                useCase.execute(ifoodOrderId, OrderStatus.CANCELADO)
        );
        verify(repo, never()).save(any());
    }

    @Test
    void shouldUpdateStatusToCanceladoAndCancelTasksAndPayments() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setExternalOrderId(ifoodOrderId);
        assertEquals(OrderStatus.ANALISE, order.getStatus());

        when(repo.findByExternalOrderId(ifoodOrderId)).thenReturn(Optional.of(order));
        when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(ifoodOrderId, OrderStatus.CANCELADO);

        assertEquals(OrderStatus.CANCELADO, order.getStatus());
        verify(repo).save(order);
        verify(cancelProductionTasksByOrderUseCase).execute(eq(storeId), any());
    }

    @Test
    void shouldUpdateStatusToPreparo() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setExternalOrderId(ifoodOrderId);
        assertEquals(OrderStatus.ANALISE, order.getStatus());

        when(repo.findByExternalOrderId(ifoodOrderId)).thenReturn(Optional.of(order));
        when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(ifoodOrderId, OrderStatus.PREPARO);

        assertEquals(OrderStatus.PREPARO, order.getStatus());
        verify(repo).save(order);
    }

    @Test
    void shouldBeIdempotentWhenStatusIsAlreadyTheSame() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setExternalOrderId(ifoodOrderId);
        order.changeStatus(OrderStatus.PREPARO);

        when(repo.findByExternalOrderId(ifoodOrderId)).thenReturn(Optional.of(order));

        useCase.execute(ifoodOrderId, OrderStatus.PREPARO);

        verify(repo, never()).save(any());
    }

    @Test
    void shouldNotRevertCancelledOrderToPreparo() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setExternalOrderId(ifoodOrderId);
        order.changeStatus(OrderStatus.CANCELADO);

        when(repo.findByExternalOrderId(ifoodOrderId)).thenReturn(Optional.of(order));

        useCase.execute(ifoodOrderId, OrderStatus.PREPARO);

        assertEquals(OrderStatus.CANCELADO, order.getStatus());
        verify(repo, never()).save(any());
    }
}

