package com.fidecode.beebite.application.kds;

import com.fidecode.beebite.application.kds.dto.ProductionTaskDtoMapper;
import com.fidecode.beebite.application.kds.dto.ProductionTaskResponse;
import com.fidecode.beebite.domain.kds.entity.KdsStation;
import com.fidecode.beebite.domain.kds.exception.KdsStationNotFoundException;
import com.fidecode.beebite.domain.kds.repository.KdsStationRepository;
import com.fidecode.beebite.domain.kds.repository.ProductionTaskRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListCompletedProductionTasksUseCase {

    private final ProductionTaskRepository taskRepository;
    private final KdsStationRepository stationRepository;

    @Inject
    public ListCompletedProductionTasksUseCase(ProductionTaskRepository taskRepository,
                                               KdsStationRepository stationRepository) {
        this.taskRepository = taskRepository;
        this.stationRepository = stationRepository;
    }

    public List<ProductionTaskResponse> execute(UUID storeId, UUID stationId, int limit) {
        Collection<UUID> sectorIds = List.of();
        boolean isExpedition = false;

        if (stationId != null) {
            KdsStation station = stationRepository.findById(stationId)
                    .orElseThrow(() -> new KdsStationNotFoundException(stationId.toString()));
            StoreGuard.enforce(station, storeId);
            sectorIds = station.getSectorIds();
            isExpedition = station.isExpedition();
        }

        int max = limit > 0 ? limit : 50;
        LocalDateTime since = LocalDateTime.now().minusHours(12);

        return taskRepository.findCompletedTasks(storeId, sectorIds, isExpedition, since, max).stream()
                .map(ProductionTaskDtoMapper::toResponse)
                .collect(Collectors.toList());
    }
}

