package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.payment.entity.Payment;
import com.trinusphere.beebite.domain.payment.repository.PaymentRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.PaymentEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.PaymentMapper;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PanachePaymentRepository implements PaymentRepository {

    private final PaymentPanacheRepo repo;
    private final com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;
    private final com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer payloadSerializer;

    public PanachePaymentRepository(PaymentPanacheRepo repo) {
        this(repo, null, null);
    }

    @Inject
    public PanachePaymentRepository(
            PaymentPanacheRepo repo,
            com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase,
            com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer payloadSerializer) {
        this.repo = repo;
        this.enqueueEdgeOutboxUseCase = enqueueEdgeOutboxUseCase;
        this.payloadSerializer = payloadSerializer;
    }

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentMapper.toEntity(payment);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        Payment saved = PaymentMapper.toDomain(entity);
        if (enqueueEdgeOutboxUseCase != null && payloadSerializer != null && saved.getId() != null) {
            enqueueEdgeOutboxUseCase.execute(
                    saved.getStoreId(),
                    "PAYMENT_UPSERT",
                    saved.getId(),
                    payloadSerializer.serializePayment(saved)
            );
        }
        return saved;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return repo.findByIdOptional(id).map(PaymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByOrderId(UUID orderId) {
        return repo.find("orderId = ?1 order by paidAt asc", orderId).list().stream()
                .map(PaymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByCashSessionId(UUID cashSessionId) {
        return repo.find("cashSessionId = ?1 order by paidAt asc", cashSessionId).list().stream()
                .map(PaymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByDriverIdAndSessionId(UUID driverId, UUID cashSessionId) {
        return repo.find("driverId = :driverId and cashSessionId = :sessionId order by paidAt asc",
                Parameters.with("driverId", driverId).and("sessionId", cashSessionId))
                .list().stream()
                .map(PaymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByStoreIdAndPeriod(UUID storeId, LocalDateTime start, LocalDateTime end) {
        return repo.find("storeId = :storeId and paidAt >= :start and paidAt <= :end order by paidAt desc",
                Parameters.with("storeId", storeId).and("start", start).and("end", end))
                .list().stream()
                .map(PaymentMapper::toDomain)
                .collect(Collectors.toList());
    }
}
