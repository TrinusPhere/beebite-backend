package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.StoreEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.StoreMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheStoreRepository implements StoreRepository {

    @Inject
    StorePanacheRepo repo;

    @Inject
    StoreCatalogContextSupport catalogContextSupport;

    @Inject
    com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;

    @Inject
    com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer payloadSerializer;

    @Override
    public Optional<Store> findById(UUID id) {
        return repo.findByIdOptional(id).map(StoreMapper::toDomain);
    }

    @Override
    public Optional<Store> findByPublicIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank())
            return Optional.empty();
        return repo.find("lower(publicSlug) = ?1 or lower(name) = ?1", identifier.toLowerCase()).firstResultOptional()
                .map(StoreMapper::toDomain);
    }

    @Override
    public List<Store> findAll() {
        return repo.findAll().stream().map(StoreMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Store save(Store store) {
        StoreEntity entity = StoreMapper.toEntity(store);
        if (entity.catalogId == null) {
            if (entity.id != null) {
                StoreEntity existing = repo.findById(entity.id);
                if (existing != null && existing.catalogId != null) {
                    entity.catalogId = existing.catalogId;
                }
            }
            if (entity.catalogId == null && catalogContextSupport != null) {
                entity.catalogId = catalogContextSupport.ensureCatalogIdForNewStore(
                        entity.tenantId, entity.name, null);
            }
        }
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        Store saved = StoreMapper.toDomain(entity);
        if (enqueueEdgeOutboxUseCase != null && payloadSerializer != null && saved.getId() != null) {
            enqueueEdgeOutboxUseCase.execute(
                    saved.getId(),
                    "STORE_STATUS_UPSERT",
                    saved.getId(),
                    payloadSerializer.serializeStoreStatus(saved)
            );
        }
        return saved;
    }
}
