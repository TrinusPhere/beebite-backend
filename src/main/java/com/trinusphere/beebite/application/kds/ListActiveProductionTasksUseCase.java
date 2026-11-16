package com.trinusphere.beebite.application.kds;

import com.trinusphere.beebite.application.kds.dto.ProductionTaskDtoMapper;
import com.trinusphere.beebite.application.kds.dto.ProductionTaskResponse;
import com.trinusphere.beebite.domain.kds.entity.KdsStation;
import com.trinusphere.beebite.domain.kds.exception.KdsStationNotFoundException;
import com.trinusphere.beebite.domain.kds.repository.KdsStationRepository;
import com.trinusphere.beebite.domain.kds.repository.ProductionTaskRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListActiveProductionTasksUseCase {

    private final ProductionTaskRepository taskRepository;
    private final KdsStationRepository stationRepository;

    @Inject
    public ListActiveProductionTasksUseCase(ProductionTaskRepository taskRepository,
                                            KdsStationRepository stationRepository) {
        this.taskRepository = taskRepository;
        this.stationRepository = stationRepository;
    }

    public List<ProductionTaskResponse> execute(UUID storeId, UUID stationId) {
        Collection<UUID> sectorIds = List.of();
        boolean isExpedition = false;

        if (stationId != null) {
            KdsStation station = stationRepository.findById(stationId)
                    .orElseThrow(() -> new KdsStationNotFoundException(stationId.toString()));
            StoreGuard.enforce(station, storeId);
            sectorIds = station.getSectorIds();
            isExpedition = station.isExpedition();
        }

        return taskRepository.findActiveTasks(storeId, sectorIds, isExpedition).stream()
                .map(ProductionTaskDtoMapper::toResponse)
                .collect(Collectors.toList());
    }
}

