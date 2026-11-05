package com.fidecode.biteflow.application.kds;

import com.fidecode.biteflow.application.kds.dto.KdsStationDtoMapper;
import com.fidecode.biteflow.application.kds.dto.KdsStationRequest;
import com.fidecode.biteflow.application.kds.dto.KdsStationResponse;
import com.fidecode.biteflow.domain.kds.entity.KdsStation;
import com.fidecode.biteflow.domain.kds.exception.KdsStationNotFoundException;
import com.fidecode.biteflow.domain.kds.repository.KdsStationRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
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

