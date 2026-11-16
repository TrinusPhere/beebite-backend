package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.kds.entity.ProductionTask;
import com.trinusphere.beebite.domain.kds.repository.ProductionTaskRepository;
import com.trinusphere.beebite.domain.kds.valueobject.TaskStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ProductionTaskEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.ProductionTaskMapper;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class PanacheProductionTaskRepository implements ProductionTaskRepository {

    private final ProductionTaskPanacheRepo repo;

    @Inject
    public PanacheProductionTaskRepository(ProductionTaskPanacheRepo repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public ProductionTask save(ProductionTask task) {
        ProductionTaskEntity entity = ProductionTaskMapper.toEntity(task);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return ProductionTaskMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public List<ProductionTask> saveAll(Collection<ProductionTask> tasks) {
        List<ProductionTask> result = new ArrayList<>();
        for (ProductionTask task : tasks) {
            result.add(save(task));
        }
        return result;
    }

    @Override
    public Optional<ProductionTask> findById(UUID id) {
        return repo.find("id = ?1 and deletedAt is null", id).firstResultOptional()
                .map(ProductionTaskMapper::toDomain);
    }

    @Override
    public List<ProductionTask> findActiveTasks(UUID storeId, Collection<UUID> sectorIds, boolean isExpedition) {
        List<String> activeStatuses = List.of(TaskStatus.PENDING.name(), TaskStatus.IN_PREPARATION.name());

        if (isExpedition || sectorIds == null || sectorIds.isEmpty()) {
            return repo.find("storeId = :storeId and status in (:statuses) and deletedAt is null order by createdAt asc",
                    Parameters.with("storeId", storeId).and("statuses", activeStatuses))
                    .list().stream()
                    .map(ProductionTaskMapper::toDomain)
                    .collect(Collectors.toList());
        }

        return repo.find("storeId = :storeId and status in (:statuses) and sectorId in (:sectorIds) and deletedAt is null order by createdAt asc",
                Parameters.with("storeId", storeId).and("statuses", activeStatuses).and("sectorIds", sectorIds))
                .list().stream()
                .map(ProductionTaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductionTask> findCompletedTasks(UUID storeId, Collection<UUID> sectorIds, boolean isExpedition, LocalDateTime since, int limit) {
        String readyStatus = TaskStatus.READY.name();

        if (isExpedition || sectorIds == null || sectorIds.isEmpty()) {
            return repo.find("storeId = :storeId and status = :status and completedAt >= :since and deletedAt is null order by completedAt desc",
                    Parameters.with("storeId", storeId).and("status", readyStatus).and("since", since))
                    .page(0, limit)
                    .list().stream()
                    .map(ProductionTaskMapper::toDomain)
                    .collect(Collectors.toList());
        }

        return repo.find("storeId = :storeId and status = :status and sectorId in (:sectorIds) and completedAt >= :since and deletedAt is null order by completedAt desc",
                Parameters.with("storeId", storeId).and("status", readyStatus).and("sectorIds", sectorIds).and("since", since))
                .page(0, limit)
                .list().stream()
                .map(ProductionTaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductionTask> findByOrderId(UUID orderId) {
        return repo.find("orderId = ?1 and deletedAt is null order by createdAt asc", orderId).list().stream()
                .map(ProductionTaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelAllByOrderId(UUID orderId) {
        repo.update("status = :status, canceledAt = :now where orderId = :orderId and status not in (:closedStatuses)",
                Parameters.with("status", TaskStatus.CANCELED.name())
                        .and("now", LocalDateTime.now())
                        .and("orderId", orderId)
                        .and("closedStatuses", List.of(TaskStatus.READY.name(), TaskStatus.CANCELED.name())));
    }
}
