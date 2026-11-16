package com.trinusphere.beebite.application.order;

import com.trinusphere.beebite.application.order.port.BiteflowEventPublisher;
import com.trinusphere.beebite.application.kds.CancelProductionTasksByOrderUseCase;
import com.trinusphere.beebite.application.kds.DispatchOrderToKdsUseCase;
import com.trinusphere.beebite.application.order.dto.OrderResponse;
import com.trinusphere.beebite.application.shared.port.TransactionExecutor;
import com.trinusphere.beebite.domain.kds.entity.KdsSettings;
import com.trinusphere.beebite.domain.kds.repository.KdsSettingsRepository;
import com.trinusphere.beebite.domain.kds.valueobject.DispatchMode;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.exception.OrderNotFoundException;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.order.service.OrderDomainService;
import com.trinusphere.beebite.domain.order.valueobject.OrderStatus;
import com.trinusphere.beebite.domain.payment.repository.PaymentRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class AdvanceOrderStatusUseCase {

    private final OrderRepository repo;
    private final OrderDomainService service;
    private final OrderFinalizationPublisher publisher;
    private final TransactionExecutor tx;
    private final DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase;
    private final CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase;
    private final KdsSettingsRepository kdsSettingsRepository;
    private final PaymentRepository paymentRepository;
    private final BiteflowEventPublisher eventPublisher;

    @Inject
    public AdvanceOrderStatusUseCase(OrderRepository repo,
                                     OrderDomainService service,
                                     OrderFinalizationPublisher publisher,
                                     TransactionExecutor tx,
                                     DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase,
                                     CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase,
                                     KdsSettingsRepository kdsSettingsRepository,
                                     PaymentRepository paymentRepository,
                                     BiteflowEventPublisher eventPublisher) {
        this.repo = repo;
        this.service = service;
        this.publisher = publisher;
        this.tx = tx;
        this.dispatchOrderToKdsUseCase = dispatchOrderToKdsUseCase;
        this.cancelProductionTasksByOrderUseCase = cancelProductionTasksByOrderUseCase;
        this.kdsSettingsRepository = kdsSettingsRepository;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    public OrderResponse execute(UUID storeId, UUID id) {
        Order saved = tx.execute(() -> {
            Order order = StoreGuard.enforce(
                    repo.findById(id).orElseThrow(() -> new OrderNotFoundException(id.toString())),
                    storeId
            );

            service.advanceStatus(order);
            Order result = repo.save(order);
            publisher.publishIfFinalized(result);

            if (result.getStatus() == OrderStatus.PREPARO && dispatchOrderToKdsUseCase != null) {
                KdsSettings settings = kdsSettingsRepository != null
                        ? kdsSettingsRepository.findByStoreId(storeId).orElseGet(() -> KdsSettings.createDefault(storeId))
                        : KdsSettings.createDefault(storeId);

                if (settings.getDispatchMode() == DispatchMode.AUTO) {
                    dispatchOrderToKdsUseCase.execute(storeId, result.getId());
                }
            } else if (result.getStatus() == OrderStatus.EM_ROTA || result.getStatus() == OrderStatus.ENTREGUE || result.getStatus() == OrderStatus.FINALIZADO || result.getStatus() == OrderStatus.CANCELADO) {
                if (cancelProductionTasksByOrderUseCase != null) {
                    cancelProductionTasksByOrderUseCase.execute(storeId, result.getId());
                }
                if (result.getStatus() == OrderStatus.CANCELADO && paymentRepository != null) {
                    paymentRepository.findByOrderId(result.getId()).forEach(p -> {
                        p.cancel();
                        paymentRepository.save(p);
                    });
                }
            }

            return result;
        });

        if (eventPublisher != null) {
            eventPublisher.publishStatusChanged(saved);
        }

        return OrderDtoMapper.toResponse(saved);
    }
}
