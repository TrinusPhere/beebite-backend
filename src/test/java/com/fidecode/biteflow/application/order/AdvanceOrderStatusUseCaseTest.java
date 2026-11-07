package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.kds.CancelProductionTasksByOrderUseCase;
import com.fidecode.biteflow.application.kds.DispatchOrderToKdsUseCase;
import com.fidecode.biteflow.application.shared.port.TransactionExecutor;
import com.fidecode.biteflow.domain.kds.entity.KdsSettings;
import com.fidecode.biteflow.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.biteflow.domain.kds.valueobject.DispatchMode;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvanceOrderStatusUseCaseTest {

    @Mock
    private OrderRepository repo;
    @Mock
    private OrderDomainService service;
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

    private AdvanceOrderStatusUseCase useCase;

    private final UUID storeId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TransactionExecutor tx = Supplier::get;
        useCase = new AdvanceOrderStatusUseCase(
                repo, service, publisher, tx,
                dispatchOrderToKdsUseCase, cancelProductionTasksByOrderUseCase,
                kdsSettingsRepository, paymentRepository
        );
    }

    @Test
    void deveCancelarTarefasKdsAoAvancarParaEmRota() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setId(orderId);
        order.changeStatus(OrderStatus.PREPARO);

        when(repo.findById(orderId)).thenReturn(Optional.of(order));
        doAnswer(inv -> {
            order.changeStatus(OrderStatus.EM_ROTA);
            return null;
        }).when(service).advanceStatus(order);
        when(repo.save(order)).thenReturn(order);

        useCase.execute(storeId, orderId);

        verify(cancelProductionTasksByOrderUseCase).execute(storeId, orderId);
        verify(dispatchOrderToKdsUseCase, never()).execute(any(), any());
    }

    @Test
    void deveDespacharParaKdsAoAvancarParaPreparoQuandoAuto() {
        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.PIX);
        order.setId(orderId);

        when(repo.findById(orderId)).thenReturn(Optional.of(order));
        doAnswer(inv -> {
            order.changeStatus(OrderStatus.PREPARO);
            return null;
        }).when(service).advanceStatus(order);
        when(repo.save(order)).thenReturn(order);
        when(kdsSettingsRepository.findByStoreId(storeId)).thenReturn(Optional.of(KdsSettings.createDefault(storeId)));

        useCase.execute(storeId, orderId);

        verify(dispatchOrderToKdsUseCase).execute(storeId, orderId);
        verify(cancelProductionTasksByOrderUseCase, never()).execute(any(), any());
    }
}
