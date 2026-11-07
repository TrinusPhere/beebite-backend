package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.port.BiteflowEventPublisher;
import com.fidecode.biteflow.application.kds.CancelProductionTasksByOrderUseCase;
import com.fidecode.biteflow.application.kds.DispatchOrderToKdsUseCase;
import com.fidecode.biteflow.application.order.dto.MoveOrderRequest;
import com.fidecode.biteflow.application.shared.port.TransactionExecutor;
import com.fidecode.biteflow.domain.kds.entity.KdsSettings;
import com.fidecode.biteflow.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.biteflow.domain.kds.valueobject.DispatchMode;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.BatchOrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class BatchMoveOrdersUseCase {
    private final OrderRepository repo;
    private final OrderDomainService service;
    private final OrderFinalizationPublisher publisher;
    private final TransactionExecutor tx;
    private final CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase;
    private final DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase;
    private final KdsSettingsRepository kdsSettingsRepository;
    private final PaymentRepository paymentRepository;
    private final BiteflowEventPublisher eventPublisher;

    @Inject
    public BatchMoveOrdersUseCase(OrderRepository repo, OrderDomainService service,
            OrderFinalizationPublisher publisher, TransactionExecutor tx,
            CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase,
            DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase,
            KdsSettingsRepository kdsSettingsRepository,
            PaymentRepository paymentRepository,
            BiteflowEventPublisher eventPublisher) {
        this.repo = repo;
        this.service = service;
        this.publisher = publisher;
        this.tx = tx;
        this.cancelProductionTasksByOrderUseCase = cancelProductionTasksByOrderUseCase;
        this.dispatchOrderToKdsUseCase = dispatchOrderToKdsUseCase;
        this.kdsSettingsRepository = kdsSettingsRepository;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(UUID storeId, List<String> orderIds, MoveOrderRequest request) {
        if (orderIds == null || orderIds.isEmpty())
            return;
        List<Order> savedOrders = tx.execute(() -> {
            var ids = orderIds.stream().map(UUID::fromString).toList();
            var orders = repo.findAllById(ids);
            if (orders.size() != ids.size())
                throw new BatchOrderNotFoundException();
            var target = OrderStatus.valueOf(request.targetStatus());
            List<Order> list = new ArrayList<>();
            for (var order : orders) {
                StoreGuard.enforce(order, storeId);
                service.moveToColumn(order, target);
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
                list.add(saved);
            }
            return list;
        });

        if (eventPublisher != null && savedOrders != null) {
            for (Order o : savedOrders) {
                eventPublisher.publishStatusChanged(o);
            }
        }
    }
}
