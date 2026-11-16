package com.trinusphere.beebite.application.kds;

import com.trinusphere.beebite.application.kds.dto.KdsStationDtoMapper;
import com.trinusphere.beebite.application.kds.dto.KdsStationRequest;
import com.trinusphere.beebite.application.kds.dto.KdsStationResponse;
import com.trinusphere.beebite.domain.kds.entity.KdsStation;
import com.trinusphere.beebite.domain.kds.exception.KdsStationNotFoundException;
import com.trinusphere.beebite.domain.kds.repository.KdsStationRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UpdateKdsStationUseCase {

    private final KdsStationRepository stationRepository;

    @Inject
    public UpdateKdsStationUseCase(KdsStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public KdsStationResponse execute(UUID storeId, UUID stationId, KdsStationRequest request) {
        KdsStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new KdsStationNotFoundException(stationId.toString()));
        StoreGuard.enforce(station, storeId);

        List<UUID> sectors = request.sectorIds() != null
                ? request.sectorIds().stream().map(UUID::fromString).collect(Collectors.toList())
                : List.of();

        station.update(request.name(), request.isExpedition(), sectors);
        KdsStation saved = stationRepository.save(station);
        return KdsStationDtoMapper.toResponse(saved);
    }
}

