package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.kds.CancelProductionTasksByOrderUseCase;
import com.fidecode.biteflow.application.kds.DispatchOrderToKdsUseCase;
import com.fidecode.biteflow.application.order.dto.OrderResponse;
import com.fidecode.biteflow.application.shared.port.TransactionExecutor;
import com.fidecode.biteflow.domain.kds.entity.KdsSettings;
import com.fidecode.biteflow.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.biteflow.domain.kds.valueobject.DispatchMode;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.shared.StoreGuard;
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

    @Inject
    public AdvanceOrderStatusUseCase(OrderRepository repo,
                                     OrderDomainService service,
                                     OrderFinalizationPublisher publisher,
                                     TransactionExecutor tx,
                                     DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase,
                                     CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase,
                                     KdsSettingsRepository kdsSettingsRepository) {
        this.repo = repo;
        this.service = service;
        this.publisher = publisher;
        this.tx = tx;
        this.dispatchOrderToKdsUseCase = dispatchOrderToKdsUseCase;
        this.cancelProductionTasksByOrderUseCase = cancelProductionTasksByOrderUseCase;
        this.kdsSettingsRepository = kdsSettingsRepository;
    }

    public OrderResponse execute(UUID storeId, UUID id) {
        return tx.execute(() -> {
            Order order = StoreGuard.enforce(
                    repo.findById(id).orElseThrow(() -> new OrderNotFoundException(id.toString())),
                    storeId
            );

            service.advanceStatus(order);
            Order saved = repo.save(order);
            publisher.publishIfFinalized(saved);

            if (saved.getStatus() == OrderStatus.PREPARO && dispatchOrderToKdsUseCase != null) {
                KdsSettings settings = kdsSettingsRepository != null
                        ? kdsSettingsRepository.findByStoreId(storeId).orElseGet(() -> KdsSettings.createDefault(storeId))
                        : KdsSettings.createDefault(storeId);

                if (settings.getDispatchMode() == DispatchMode.AUTO) {
                    dispatchOrderToKdsUseCase.execute(storeId, saved.getId());
                }
            } else if (saved.getStatus() == OrderStatus.CANCELADO && cancelProductionTasksByOrderUseCase != null) {
                cancelProductionTasksByOrderUseCase.execute(storeId, saved.getId());
            }

            return OrderDtoMapper.toResponse(saved);
        });
    }
}
