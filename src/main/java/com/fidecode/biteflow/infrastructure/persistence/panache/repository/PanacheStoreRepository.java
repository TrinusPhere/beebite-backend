package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.store.entity.Store;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.StoreEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.StoreMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementação do port StoreRepository (domínio). Usa StorePanacheRepo internamente — padrão Repository do Panache.
 */
@ApplicationScoped
public class PanacheStoreRepository implements StoreRepository {

    @Inject
    StorePanacheRepo repo;

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
    @Transactional
    public Store save(Store store) {
        StoreEntity entity = StoreMapper.toEntity(store);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return StoreMapper.toDomain(entity);
    }
}
