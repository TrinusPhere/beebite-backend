package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.repository.CashSessionRepository;
import com.trinusphere.beebite.domain.cash.valueobject.CashSessionStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CashSessionEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.CashSessionMapper;
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
public class PanacheCashSessionRepository implements CashSessionRepository {

    private final CashSessionPanacheRepo repo;
    private final com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;
    private final com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer payloadSerializer;

    public PanacheCashSessionRepository(CashSessionPanacheRepo repo) {
        this(repo, null, null);
    }

    @Inject
    public PanacheCashSessionRepository(
            CashSessionPanacheRepo repo,
            com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase,
            com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer payloadSerializer) {
        this.repo = repo;
        this.enqueueEdgeOutboxUseCase = enqueueEdgeOutboxUseCase;
        this.payloadSerializer = payloadSerializer;
    }

    @Override
    @Transactional
    public CashSession save(CashSession session) {
        CashSessionEntity entity = CashSessionMapper.toEntity(session);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        CashSession saved = CashSessionMapper.toDomain(entity);
        if (enqueueEdgeOutboxUseCase != null && payloadSerializer != null && saved.getId() != null) {
            enqueueEdgeOutboxUseCase.execute(
                    saved.getStoreId(),
                    "CASH_SESSION_UPSERT",
                    saved.getId(),
                    payloadSerializer.serializeCashSession(saved)
            );
        }
        return saved;
    }

    @Override
    public Optional<CashSession> findById(UUID id) {
        return repo.findByIdOptional(id).map(CashSessionMapper::toDomain);
    }

    @Override
    public Optional<CashSession> findOpenSessionByUserId(UUID storeId, UUID userId) {
        return repo.find("storeId = :storeId and userId = :userId and status = :status",
                Parameters.with("storeId", storeId)
                        .and("userId", userId)
                        .and("status", CashSessionStatus.OPEN.name()))
                .firstResultOptional()
                .map(CashSessionMapper::toDomain);
    }

    @Override
    public Optional<CashSession> findOpenSessionByRegisterId(UUID storeId, UUID registerId) {
        return repo.find("storeId = :storeId and cashRegisterId = :registerId and status = :status",
                Parameters.with("storeId", storeId)
                        .and("registerId", registerId)
                        .and("status", CashSessionStatus.OPEN.name()))
                .firstResultOptional()
                .map(CashSessionMapper::toDomain);
    }

    @Override
    public List<CashSession> findByStoreIdAndPeriod(UUID storeId, LocalDateTime start, LocalDateTime end) {
        return repo.find("storeId = :storeId and openedAt >= :start and openedAt <= :end order by openedAt desc",
                Parameters.with("storeId", storeId).and("start", start).and("end", end))
                .list().stream()
                .map(CashSessionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashSession> findByStoreIdAndStatus(UUID storeId, CashSessionStatus status) {
        return repo.find("storeId = :storeId and status = :status order by openedAt desc",
                Parameters.with("storeId", storeId).and("status", status.name()))
                .list().stream()
                .map(CashSessionMapper::toDomain)
                .collect(Collectors.toList());
    }
}
