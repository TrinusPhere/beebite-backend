package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase;
import com.trinusphere.beebite.application.sync.port.EdgeOutboxBackfillPort;
import com.trinusphere.beebite.domain.cash.entity.CashMovement;
import com.trinusphere.beebite.domain.cash.entity.CashRegister;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.repository.CashRegisterRepository;
import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.payment.entity.Payment;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CashMovementEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CashSessionEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeOutboxEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.FinancialRecordEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.PaymentEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.CashMovementMapper;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.CashSessionMapper;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.FinancialRecordMapper;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.PaymentMapper;
import com.trinusphere.beebite.infrastructure.persistence.panache.repository.CashMovementPanacheRepo;
import com.trinusphere.beebite.infrastructure.persistence.panache.repository.CashSessionPanacheRepo;
import com.trinusphere.beebite.infrastructure.persistence.panache.repository.EdgeOutboxPanacheRepo;
import com.trinusphere.beebite.infrastructure.persistence.panache.repository.FinancialRecordPanacheRepo;
import com.trinusphere.beebite.infrastructure.persistence.panache.repository.PaymentPanacheRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Garante que operações já gravadas localmente (abertura de caixa, movimentações, pedidos com itens,
 * pagamentos e registros financeiros) sejam enfileiradas em ordem topológica na Outbox caso ainda não
 * constem nela (ex.: bancada atualizada após já ter operado).
 */
@ApplicationScoped
public class PanacheEdgeOutboxBackfillAdapter implements EdgeOutboxBackfillPort {

    private static final Logger LOG = Logger.getLogger(PanacheEdgeOutboxBackfillAdapter.class);
    private static final int BACKFILL_ORDER_WINDOW_DAYS = 30;

    @Inject
    StoreRepository storeRepository;

    @Inject
    CashRegisterRepository cashRegisterRepository;

    @Inject
    CashSessionPanacheRepo cashSessionRepo;

    @Inject
    CashMovementPanacheRepo cashMovementRepo;

    @Inject
    OrderRepository orderRepository;

    @Inject
    PaymentPanacheRepo paymentRepo;

    @Inject
    FinancialRecordPanacheRepo financialRecordRepo;

    @Inject
    EdgeOutboxPanacheRepo outboxRepo;

    @Inject
    EnqueueEdgeOutboxUseCase enqueueUseCase;

    @Inject
    EdgeSyncPayloadSerializer serializer;

    @Override
    @Transactional
    public int backfillMissingEvents(UUID storeId) {
        if (storeId == null) {
            return 0;
        }
        int enqueued = 0;

        var storeOpt = storeRepository.findById(storeId);
        if (storeOpt.isPresent() && !existsOutboxEvent(storeId, "STORE_STATUS_UPSERT", storeId)) {
            enqueueUseCase.execute(storeId, "STORE_STATUS_UPSERT", storeId, serializer.serializeStoreStatus(storeOpt.get()));
            enqueued++;
        }

        for (CashRegister register : cashRegisterRepository.findAllByStoreId(storeId)) {
            if (register.getId() != null && !existsOutboxEvent(storeId, "CASH_REGISTER_UPSERT", register.getId())) {
                enqueueUseCase.execute(storeId, "CASH_REGISTER_UPSERT", register.getId(), serializer.serializeCashRegister(register));
                enqueued++;
            }
        }

        List<CashSessionEntity> sessions = cashSessionRepo.find("storeId = ?1 order by openedAt asc", storeId).list();
        for (CashSessionEntity entity : sessions) {
            if (entity.id != null && !existsOutboxEvent(storeId, "CASH_SESSION_UPSERT", entity.id)) {
                CashSession domain = CashSessionMapper.toDomain(entity);
                enqueueUseCase.execute(storeId, "CASH_SESSION_UPSERT", entity.id, serializer.serializeCashSession(domain));
                enqueued++;
            }
        }

        List<CashMovementEntity> movements = cashMovementRepo.find("storeId = ?1 order by createdAt asc", storeId).list();
        for (CashMovementEntity entity : movements) {
            if (entity.id != null && !existsOutboxEvent(storeId, "CASH_MOVEMENT_UPSERT", entity.id)) {
                CashMovement domain = CashMovementMapper.toDomain(entity);
                enqueueUseCase.execute(storeId, "CASH_MOVEMENT_UPSERT", entity.id, serializer.serializeCashMovement(domain));
                enqueued++;
            }
        }

        java.time.LocalDateTime backfillStart = java.time.LocalDateTime.now().minusDays(BACKFILL_ORDER_WINDOW_DAYS);
        java.time.LocalDateTime backfillEnd = java.time.LocalDateTime.now().plusDays(1);
        for (Order order : orderRepository.findByStoreIdAndDateRange(storeId, backfillStart, backfillEnd)) {
            if (order.getId() == null) {
                continue;
            }
            String fullOrderJson = serializer.serializeOrder(order);
            List<EdgeOutboxEntity> existingOrderEvents = outboxRepo.find(
                    "storeId = ?1 and eventType = 'ORDER_UPSERT' and aggregateId = ?2",
                    storeId, order.getId()).list();

            if (existingOrderEvents.isEmpty()) {
                enqueueUseCase.execute(storeId, "ORDER_UPSERT", order.getId(), fullOrderJson);
                enqueued++;
            } else {
                for (EdgeOutboxEntity ev : existingOrderEvents) {
                    if ("PENDING".equals(ev.status) && (ev.payload == null || !ev.payload.contains("\"items\""))) {
                        ev.payload = fullOrderJson;
                        enqueued++;
                    }
                }
            }
        }

        List<PaymentEntity> payments = paymentRepo.find("storeId = ?1 order by createdAt asc", storeId).list();
        for (PaymentEntity entity : payments) {
            if (entity.id != null && !existsOutboxEvent(storeId, "PAYMENT_UPSERT", entity.id)) {
                Payment domain = PaymentMapper.toDomain(entity);
                enqueueUseCase.execute(storeId, "PAYMENT_UPSERT", entity.id, serializer.serializePayment(domain));
                enqueued++;
            }
        }

        List<FinancialRecordEntity> records = financialRecordRepo.find("storeId = ?1 order by createdAt asc", storeId).list();
        for (FinancialRecordEntity entity : records) {
            if (entity.id != null && !existsOutboxEvent(storeId, "FINANCIAL_RECORD_UPSERT", entity.id)) {
                FinancialRecord domain = FinancialRecordMapper.toDomain(entity);
                enqueueUseCase.execute(storeId, "FINANCIAL_RECORD_UPSERT", entity.id, serializer.serializeFinancialRecord(domain));
                enqueued++;
            }
        }

        if (enqueued > 0) {
            LOG.infof("[EDGE-BACKFILL] %d evento(s) operacional(is) preparados na fila de sincronização para a loja %s.",
                    enqueued, storeId);
        }
        return enqueued;
    }

    private boolean existsOutboxEvent(UUID storeId, String eventType, UUID aggregateId) {
        return outboxRepo.count("storeId = ?1 and eventType = ?2 and aggregateId = ?3",
                storeId, eventType, aggregateId) > 0;
    }
}
