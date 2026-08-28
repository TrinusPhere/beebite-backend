package com.fidecode.biteflow.domain.store.repository;

import com.fidecode.biteflow.domain.store.entity.Store;

import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) — StoreRepository. Independente de framework; implementado na camada de infraestrutura.
 */
public interface StoreRepository {
    Optional<Store> findById(UUID id);

    Optional<Store> findByPublicIdentifier(String identifier);

    Store save(Store store);
}
