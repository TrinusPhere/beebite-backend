package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase;
import com.trinusphere.beebite.domain.catalog.entity.Additional;
import com.trinusphere.beebite.domain.catalog.repository.AdditionalRepository;
import com.trinusphere.beebite.domain.shared.PageResult;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.AdditionalEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.AdditionalMapper;
import com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheAdditionalRepository implements AdditionalRepository {

    @Inject
    AdditionalPanacheRepo repo;

    @Inject
    StoreCatalogContextSupport catalogSupport;

    @Inject
    EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;

    @Inject
    EdgeSyncPayloadSerializer payloadSerializer;

    @Override
    public Optional<Additional> findById(UUID id) {
        return repo.findByIdOptional(id).map(AdditionalMapper::toDomain);
    }

    @Override
    public Optional<Additional> findByIdInStore(UUID id, UUID storeId) {
        if (id == null) {
            return Optional.empty();
        }
        Optional<AdditionalEntity> entityOpt = repo.findByIdOptional(id);
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }
        AdditionalEntity entity = entityOpt.get();
        UUID expectedCatalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        UUID contextStore = (expectedCatalogId != null && expectedCatalogId.equals(entity.catalogId))
                ? storeId
                : StoreCatalogContextSupport.FOREIGN_CATALOG_SENTINEL;
        return Optional.of(AdditionalMapper.toDomain(entity, contextStore));
    }

    @Override
    @Transactional
    public PageResult<Additional> findAllByStoreId(UUID storeId, int page, int size) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        var query = repo.find("catalogId", catalogId).page(Page.of(page, size));
        List<Additional> content = query.list().stream()
                .map(e -> AdditionalMapper.toDomain(e, storeId))
                .toList();
        return new PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    @Transactional
    public Additional save(Additional additional) {
        UUID contextStoreId = catalogSupport.isStoreIdKnown(additional.getStoreId()) ? additional.getStoreId() : null;
        UUID catalogId = contextStoreId != null
                ? catalogSupport.resolveCatalogIdForStore(contextStoreId)
                : catalogSupport.resolveCatalogIdForStore(additional.getCatalogId());
        additional.assignCatalog(catalogId);

        AdditionalEntity entity = AdditionalMapper.toEntity(additional, catalogId);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        repo.getEntityManager().flush();

        Additional saved = AdditionalMapper.toDomain(entity, contextStoreId);
        enqueueOutboxIfEdge(saved, entity.updatedAt, null, contextStoreId);
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            LocalDateTime now = LocalDateTime.now();
            entity.deletedAt = now;
            entity.updatedAt = Instant.now();
            repo.getEntityManager().merge(entity);
            Additional deleted = AdditionalMapper.toDomain(entity);
            enqueueOutboxIfEdge(deleted, entity.updatedAt, now, null);
        });
    }

    private void enqueueOutboxIfEdge(Additional additional, Instant updatedAt, LocalDateTime deletedAt, UUID preferredStoreId) {
        if (enqueueEdgeOutboxUseCase == null || payloadSerializer == null || additional.getId() == null) {
            return;
        }
        UUID outboxStoreId = catalogSupport.resolveRepresentativeStoreIdForCatalog(additional.getCatalogId(), preferredStoreId);
        if (outboxStoreId != null) {
            enqueueEdgeOutboxUseCase.execute(
                    outboxStoreId,
                    "CATALOG_ADDITIONAL_UPSERT",
                    additional.getId(),
                    payloadSerializer.serializeCatalogAdditional(additional, updatedAt, deletedAt)
            );
        }
    }
}
