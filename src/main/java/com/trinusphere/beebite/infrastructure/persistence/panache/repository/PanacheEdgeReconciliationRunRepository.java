package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeReconciliationRun;
import com.trinusphere.beebite.domain.sync.repository.EdgeReconciliationRunRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeReconciliationRunEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.EdgeReconciliationRunMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheEdgeReconciliationRunRepository implements EdgeReconciliationRunRepository {

    @Inject
    EdgeReconciliationRunPanacheRepo repo;

    @Override
    @Transactional
    public EdgeReconciliationRun save(EdgeReconciliationRun run) {
        EdgeReconciliationRunEntity entity = EdgeReconciliationRunMapper.toEntity(run);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return EdgeReconciliationRunMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public Optional<EdgeReconciliationRun> findLatestByStoreId(UUID storeId) {
        return repo.find("storeId = ?1 order by startedAt desc", storeId)
                .firstResultOptional()
                .map(EdgeReconciliationRunMapper::toDomain);
    }
}

