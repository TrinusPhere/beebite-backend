package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase;
import com.trinusphere.beebite.domain.catalog.entity.ModifierGroup;
import com.trinusphere.beebite.domain.catalog.repository.ModifierGroupRepository;
import com.trinusphere.beebite.domain.shared.PageResult;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ModifierGroupEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.ModifierGroupMapper;
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
public class PanacheModifierGroupRepository implements ModifierGroupRepository {

    @Inject
    ModifierGroupPanacheRepo repo;

    @Inject
    StoreCatalogContextSupport catalogSupport;

    @Inject
    EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;

    @Inject
    EdgeSyncPayloadSerializer payloadSerializer;

    @Override
    @Transactional
    public Optional<ModifierGroup> findById(UUID id) {
        return repo.findByIdOptional(id).map(ModifierGroupMapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<ModifierGroup> findByIdInStore(UUID id, UUID storeId) {
        if (id == null) {
            return Optional.empty();
        }
        Optional<ModifierGroupEntity> entityOpt = repo.findByIdOptional(id);
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }
        ModifierGroupEntity entity = entityOpt.get();
        UUID expectedCatalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        UUID contextStore = (expectedCatalogId != null && expectedCatalogId.equals(entity.catalogId))
                ? storeId
                : StoreCatalogContextSupport.FOREIGN_CATALOG_SENTINEL;
        return Optional.of(ModifierGroupMapper.toDomain(entity, contextStore));
    }

    @Override
    @Transactional
    public PageResult<ModifierGroup> findAllByStoreId(UUID storeId, int page, int size) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        var query = repo.find("catalogId", catalogId).page(Page.of(page, size));
        List<ModifierGroup> content = query.list().stream()
                .map(e -> ModifierGroupMapper.toDomain(e, storeId))
                .toList();
        return new PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    @Transactional
    public List<ModifierGroup> findAllByStoreId(UUID storeId) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        return repo.find("catalogId", catalogId).list().stream()
                .map(e -> ModifierGroupMapper.toDomain(e, storeId))
                .toList();
    }

    @Override
    @Transactional
    public List<ModifierGroup> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        return repo.find("id in ?1", ids).list().stream()
                .map(ModifierGroupMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public ModifierGroup save(ModifierGroup group) {
        UUID contextStoreId = catalogSupport.isStoreIdKnown(group.getStoreId()) ? group.getStoreId() : null;
        UUID catalogId = contextStoreId != null
                ? catalogSupport.resolveCatalogIdForStore(contextStoreId)
                : catalogSupport.resolveCatalogIdForStore(group.getCatalogId());
        group.assignCatalog(catalogId);

        ModifierGroupEntity entity = ModifierGroupMapper.toEntity(group, catalogId);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        repo.getEntityManager().flush();

        ModifierGroup saved = ModifierGroupMapper.toDomain(entity, contextStoreId);
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
            ModifierGroup deleted = ModifierGroupMapper.toDomain(entity);
            enqueueOutboxIfEdge(deleted, entity.updatedAt, now, null);
        });
    }

    private void enqueueOutboxIfEdge(ModifierGroup group, Instant updatedAt, LocalDateTime deletedAt, UUID preferredStoreId) {
        if (enqueueEdgeOutboxUseCase == null || payloadSerializer == null || group.getId() == null) {
            return;
        }
        UUID outboxStoreId = catalogSupport.resolveRepresentativeStoreIdForCatalog(group.getCatalogId(), preferredStoreId);
        if (outboxStoreId != null) {
            enqueueEdgeOutboxUseCase.execute(
                    outboxStoreId,
                    "CATALOG_MODIFIER_GROUP_UPSERT",
                    group.getId(),
                    payloadSerializer.serializeCatalogModifierGroup(group, updatedAt, deletedAt)
            );
        }
    }
}
