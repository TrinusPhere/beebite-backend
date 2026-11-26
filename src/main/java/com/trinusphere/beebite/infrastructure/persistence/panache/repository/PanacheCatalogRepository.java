package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.catalog.entity.Catalog;
import com.trinusphere.beebite.domain.catalog.repository.CatalogRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CatalogEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.CatalogMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheCatalogRepository implements CatalogRepository {

    @Inject
    CatalogPanacheRepo repo;

    @Override
    @Transactional
    public Catalog save(Catalog catalog) {
        CatalogEntity entity = CatalogMapper.toEntity(catalog);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return CatalogMapper.toDomain(entity);
    }

    @Override
    public Optional<Catalog> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return repo.findByIdOptional(id).map(CatalogMapper::toDomain);
    }

    @Override
    public Optional<Catalog> findDefaultByTenantId(UUID tenantId) {
        if (tenantId == null) {
            return Optional.empty();
        }
        return repo.find("tenantId = ?1 and defaultCatalog = true order by createdAt asc", tenantId)
                .firstResultOptional()
                .or(() -> repo.find("tenantId = ?1 order by createdAt asc", tenantId).firstResultOptional())
                .map(CatalogMapper::toDomain);
    }

    @Override
    public List<Catalog> findAllByTenantId(UUID tenantId) {
        if (tenantId == null) {
            return List.of();
        }
        return repo.find("tenantId = ?1 order by defaultCatalog desc, createdAt asc", tenantId)
                .list()
                .stream()
                .map(CatalogMapper::toDomain)
                .toList();
    }
}

