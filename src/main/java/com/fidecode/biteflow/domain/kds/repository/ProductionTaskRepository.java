package com.fidecode.biteflow.domain.kds.repository;

import com.fidecode.biteflow.domain.kds.entity.ProductionTask;
import com.fidecode.biteflow.domain.kds.valueobject.TaskStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionTaskRepository {
    ProductionTask save(ProductionTask task);
    List<ProductionTask> saveAll(Collection<ProductionTask> tasks);
    Optional<ProductionTask> findById(UUID id);
    List<ProductionTask> findActiveTasks(UUID storeId, Collection<UUID> sectorIds, boolean isExpedition);
    List<ProductionTask> findCompletedTasks(UUID storeId, Collection<UUID> sectorIds, boolean isExpedition, LocalDateTime since, int limit);
    List<ProductionTask> findByOrderId(UUID orderId);
    void cancelAllByOrderId(UUID orderId);
}

