package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.kds.CancelProductionTasksByOrderUseCase;
import com.fidecode.biteflow.application.kds.DispatchOrderToKdsUseCase;
import com.fidecode.biteflow.application.order.dto.OrderResponse;
import com.fidecode.biteflow.application.shared.port.TransactionExecutor;
import com.fidecode.biteflow.domain.kds.entity.KdsSettings;
import com.fidecode.biteflow.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.biteflow.domain.kds.valueobject.DispatchMode;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class MoveOrderToColumnUseCase {
    private final OrderRepository repo;
    private final OrderDomainService service;
    private final OrderFinalizationPublisher publisher;
    private final TransactionExecutor tx;
    private final CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase;
    private final DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase;
    private final KdsSettingsRepository kdsSettingsRepository;
    private final PaymentRepository paymentRepository;

    @Inject
    public MoveOrderToColumnUseCase(OrderRepository repo, OrderDomainService service,
            OrderFinalizationPublisher publisher, TransactionExecutor tx,
            CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase,
            DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase,
            KdsSettingsRepository kdsSettingsRepository,
            PaymentRepository paymentRepository) {
        this.repo = repo;
        this.service = service;
        this.publisher = publisher;
        this.tx = tx;
        this.cancelProductionTasksByOrderUseCase = cancelProductionTasksByOrderUseCase;
        this.dispatchOrderToKdsUseCase = dispatchOrderToKdsUseCase;
        this.kdsSettingsRepository = kdsSettingsRepository;
        this.paymentRepository = paymentRepository;
    }

    public OrderResponse execute(UUID storeId, UUID id, String targetStatus) {
        return tx.execute(() -> {
            var order = StoreGuard
                    .enforce(repo.findById(id).orElseThrow(() -> new OrderNotFoundException(id.toString())), storeId);
            service.moveToColumn(order, OrderStatus.valueOf(targetStatus));
            var saved = repo.save(order);
            publisher.publishIfFinalized(saved);

            if (saved.getStatus() == OrderStatus.PREPARO && dispatchOrderToKdsUseCase != null) {
                KdsSettings settings = kdsSettingsRepository != null
                        ? kdsSettingsRepository.findByStoreId(storeId).orElseGet(() -> KdsSettings.createDefault(storeId))
                        : KdsSettings.createDefault(storeId);
                if (settings.getDispatchMode() == DispatchMode.AUTO) {
                    dispatchOrderToKdsUseCase.execute(storeId, saved.getId());
                }
            } else if (saved.getStatus() == OrderStatus.EM_ROTA || saved.getStatus() == OrderStatus.FINALIZADO || saved.getStatus() == OrderStatus.CANCELADO) {
                if (cancelProductionTasksByOrderUseCase != null) {
                    cancelProductionTasksByOrderUseCase.execute(storeId, saved.getId());
                }
                if (saved.getStatus() == OrderStatus.CANCELADO && paymentRepository != null) {
                    paymentRepository.findByOrderId(saved.getId()).forEach(p -> {
                        p.cancel();
                        paymentRepository.save(p);
                    });
                }
            }

            return OrderDtoMapper.toResponse(saved);
        });
    }
}
