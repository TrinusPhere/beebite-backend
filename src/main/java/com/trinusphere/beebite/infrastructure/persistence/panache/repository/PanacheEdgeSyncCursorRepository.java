package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncCursor;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncCursorRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeSyncCursorEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.EdgeSyncCursorMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheEdgeSyncCursorRepository implements EdgeSyncCursorRepository {

    @Inject
    EdgeSyncCursorPanacheRepo repo;

    @Override
    @Transactional
    public Optional<EdgeSyncCursor> findByStoreId(UUID storeId) {
        return repo.findByIdOptional(storeId).map(EdgeSyncCursorMapper::toDomain);
    }

    @Override
    @Transactional
    public EdgeSyncCursor save(EdgeSyncCursor cursor) {
        Optional<EdgeSyncCursorEntity> existing = repo.findByIdOptional(cursor.getStoreId());
        EdgeSyncCursorEntity entity = EdgeSyncCursorMapper.toEntity(
                cursor,
                existing.map(e -> e.initializedAt).orElse(null)
        );
        if (existing.isEmpty()) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return EdgeSyncCursorMapper.toDomain(entity);
    }
}

