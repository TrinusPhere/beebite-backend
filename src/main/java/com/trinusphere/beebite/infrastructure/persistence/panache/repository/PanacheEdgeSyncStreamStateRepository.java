package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeSyncStreamStateEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.EdgeSyncStreamStateMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheEdgeSyncStreamStateRepository implements EdgeSyncStreamStateRepository {

    @Inject
    EdgeSyncStreamStatePanacheRepo repo;

    @Override
    @Transactional
    public EdgeSyncStreamState findOrInitialize(UUID storeId) {
        return repo.findByIdOptional(storeId)
                .map(EdgeSyncStreamStateMapper::toDomain)
                .orElseGet(() -> EdgeSyncStreamState.initial(storeId));
    }

    @Override
    @Transactional
    public EdgeSyncStreamState save(EdgeSyncStreamState state) {
        Optional<EdgeSyncStreamStateEntity> existing = repo.findByIdOptional(state.getStoreId());
        EdgeSyncStreamStateEntity entity = EdgeSyncStreamStateMapper.toEntity(state);
        if (existing.isEmpty()) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return EdgeSyncStreamStateMapper.toDomain(entity);
    }
}

