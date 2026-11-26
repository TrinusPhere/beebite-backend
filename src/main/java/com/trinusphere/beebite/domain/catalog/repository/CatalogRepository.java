package com.trinusphere.beebite.domain.catalog.repository;

import com.trinusphere.beebite.domain.catalog.entity.Catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogRepository {
    Catalog save(Catalog catalog);

    Optional<Catalog> findById(UUID id);

    Optional<Catalog> findDefaultByTenantId(UUID tenantId);

    List<Catalog> findAllByTenantId(UUID tenantId);
}

