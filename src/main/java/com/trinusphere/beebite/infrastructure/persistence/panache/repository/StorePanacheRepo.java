package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.infrastructure.persistence.panache.entity.StoreEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * Repositório Panache interno para StoreEntity. Não implementa a porta de domínio — apenas fornece acesso Panache a
 * StoreEntity. Injetado em PanacheStoreRepository para uso interno.
 */
@ApplicationScoped
public class StorePanacheRepo implements PanacheRepositoryBase<StoreEntity, UUID> {
    // Herda todos os métodos Panache: findById, find, list, persist, deleteById, count, etc.
}
