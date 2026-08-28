package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.production.entity.ProductionSector;
import com.fidecode.biteflow.domain.production.repository.ProductionSectorRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.ProductionSectorMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class PanacheProductionSectorRepository implements ProductionSectorRepository {
    @Inject
    ProductionSectorPanacheRepo repo;

    public List<ProductionSector> findAllByStoreId(UUID storeId) {
        return repo.find("storeId = ?1 order by name", storeId).list().stream().map(ProductionSectorMapper::toDomain)
                .toList();
    }

    public Optional<ProductionSector> findById(UUID id) {
        return repo.findByIdOptional(id).map(ProductionSectorMapper::toDomain);
    }

    @Transactional
    public ProductionSector save(ProductionSector s) {
        var e = ProductionSectorMapper.toEntity(s);
        if (e.id == null)
            repo.persist(e);
        else
            e = repo.getEntityManager().merge(e);
        return ProductionSectorMapper.toDomain(e);
    }

    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(e -> {
            e.deletedAt = LocalDateTime.now();
        });
    }
}
