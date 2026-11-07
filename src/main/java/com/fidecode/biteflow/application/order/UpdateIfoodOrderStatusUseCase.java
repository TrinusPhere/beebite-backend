package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.kds.CancelProductionTasksByOrderUseCase;
import com.fidecode.biteflow.application.kds.DispatchOrderToKdsUseCase;
import com.fidecode.biteflow.application.shared.port.TransactionExecutor;
import com.fidecode.biteflow.domain.kds.entity.KdsSettings;
import com.fidecode.biteflow.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.biteflow.domain.kds.valueobject.DispatchMode;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UpdateIfoodOrderStatusUseCase {

    private static final Logger LOG = Logger.getLogger(UpdateIfoodOrderStatusUseCase.class);

    private final OrderRepository repo;
    private final OrderFinalizationPublisher publisher;
    private final TransactionExecutor tx;
    private final CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase;
    private final DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase;
    private final KdsSettingsRepository kdsSettingsRepository;
    private final PaymentRepository paymentRepository;

    @Inject
    public UpdateIfoodOrderStatusUseCase(OrderRepository repo,
                                         OrderFinalizationPublisher publisher,
                                         TransactionExecutor tx,
                                         DispatchOrderToKdsUseCase dispatchOrderToKdsUseCase,
                                         CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase,
                                         KdsSettingsRepository kdsSettingsRepository,
                                         PaymentRepository paymentRepository) {
        this.repo = repo;
        this.publisher = publisher;
        this.tx = tx;
        this.dispatchOrderToKdsUseCase = dispatchOrderToKdsUseCase;
        this.cancelProductionTasksByOrderUseCase = cancelProductionTasksByOrderUseCase;
        this.kdsSettingsRepository = kdsSettingsRepository;
        this.paymentRepository = paymentRepository;
    }

    public void execute(String ifoodOrderId, OrderStatus targetStatus) {
        tx.execute(() -> {
            var order = repo.findByExternalOrderId(ifoodOrderId)
                    .orElseThrow(() -> new OrderNotFoundException("externalOrderId=" + ifoodOrderId));

            if (order.getStatus() == targetStatus) {
                LOG.debugf("[IFOOD-STATUS] Pedido %s já está em status %s — ignorando.", ifoodOrderId, targetStatus);
                return null;
            }

            if (order.getStatus() == OrderStatus.CANCELADO) {
                LOG.infof("[IFOOD-STATUS] Pedido %s já está CANCELADO — ignorando novo status %s.", ifoodOrderId, targetStatus);
                return null;
            }

            if (order.getStatus() == OrderStatus.FINALIZADO && targetStatus != OrderStatus.CANCELADO) {
                LOG.infof("[IFOOD-STATUS] Pedido %s já está FINALIZADO — ignorando transição para %s.", ifoodOrderId, targetStatus);
                return null;
            }

            order.changeStatus(targetStatus);
            var saved = repo.save(order);
            publisher.publishIfFinalized(saved);

            if (saved.getStatus() == OrderStatus.PREPARO && dispatchOrderToKdsUseCase != null) {
                KdsSettings settings = kdsSettingsRepository != null
                        ? kdsSettingsRepository.findByStoreId(saved.getStoreId()).orElseGet(() -> KdsSettings.createDefault(saved.getStoreId()))
                        : KdsSettings.createDefault(saved.getStoreId());
                if (settings.getDispatchMode() == DispatchMode.AUTO) {
                    dispatchOrderToKdsUseCase.execute(saved.getStoreId(), saved.getId());
                }
            } else if (saved.getStatus() == OrderStatus.EM_ROTA || saved.getStatus() == OrderStatus.FINALIZADO || saved.getStatus() == OrderStatus.CANCELADO) {
                if (cancelProductionTasksByOrderUseCase != null) {
                    cancelProductionTasksByOrderUseCase.execute(saved.getStoreId(), saved.getId());
                }
                if (saved.getStatus() == OrderStatus.CANCELADO && paymentRepository != null) {
                    paymentRepository.findByOrderId(saved.getId()).forEach(p -> {
                        p.cancel();
                        paymentRepository.save(p);
                    });
                }
            }

            LOG.infof("[IFOOD-STATUS] Pedido %s atualizado para %s", ifoodOrderId, targetStatus);
            return null;
        });
    }
}

