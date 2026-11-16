package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.inventory.entity.StockMovement;
import com.trinusphere.beebite.domain.inventory.repository.StockMovementRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.StockMovementMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public class PanacheStockMovementRepository implements StockMovementRepository {
    @Inject
    StockMovementPanacheRepo repo;

    public List<StockMovement> findAllByStoreId(UUID storeId) {
        return repo.find("storeId = ?1 order by createdAt desc", storeId).list().stream()
                .map(StockMovementMapper::toDomain).toList();
    }

    @Transactional
    public StockMovement save(StockMovement m) {
        var e = StockMovementMapper.toEntity(m);
        repo.persist(e);
        return StockMovementMapper.toDomain(e);
    }
}
