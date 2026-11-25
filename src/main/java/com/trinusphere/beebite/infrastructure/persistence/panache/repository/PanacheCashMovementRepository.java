package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.cash.entity.CashMovement;
import com.trinusphere.beebite.domain.cash.repository.CashMovementRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CashMovementEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.CashMovementMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PanacheCashMovementRepository implements CashMovementRepository {

    private final CashMovementPanacheRepo repo;
    private final com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;
    private final com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer payloadSerializer;

    public PanacheCashMovementRepository(CashMovementPanacheRepo repo) {
        this(repo, null, null);
    }

    @Inject
    public PanacheCashMovementRepository(
            CashMovementPanacheRepo repo,
            com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase,
            com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer payloadSerializer) {
        this.repo = repo;
        this.enqueueEdgeOutboxUseCase = enqueueEdgeOutboxUseCase;
        this.payloadSerializer = payloadSerializer;
    }

    @Override
    @Transactional
    public CashMovement save(CashMovement movement) {
        CashMovementEntity entity = CashMovementMapper.toEntity(movement);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        CashMovement saved = CashMovementMapper.toDomain(entity);
        if (enqueueEdgeOutboxUseCase != null && payloadSerializer != null && saved.getId() != null) {
            enqueueEdgeOutboxUseCase.execute(
                    saved.getStoreId(),
                    "CASH_MOVEMENT_UPSERT",
                    saved.getId(),
                    payloadSerializer.serializeCashMovement(saved)
            );
        }
        return saved;
    }

    @Override
    public Optional<CashMovement> findById(UUID id) {
        return repo.findByIdOptional(id).map(CashMovementMapper::toDomain);
    }

    @Override
    public List<CashMovement> findBySessionId(UUID sessionId) {
        return repo.find("cashSessionId = ?1 order by createdAt asc", sessionId).list().stream()
                .map(CashMovementMapper::toDomain)
                .collect(Collectors.toList());
    }
}
