package com.fidecode.beebite.application.kds;

import com.fidecode.beebite.application.kds.dto.KdsStationDtoMapper;
import com.fidecode.beebite.application.kds.dto.KdsStationRequest;
import com.fidecode.beebite.application.kds.dto.KdsStationResponse;
import com.fidecode.beebite.domain.kds.entity.KdsStation;
import com.fidecode.beebite.domain.kds.repository.KdsStationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CreateKdsStationUseCase {

    private final KdsStationRepository stationRepository;

    @Inject
    public CreateKdsStationUseCase(KdsStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public KdsStationResponse execute(UUID storeId, KdsStationRequest request) {
        List<UUID> sectors = request.sectorIds() != null
                ? request.sectorIds().stream().map(UUID::fromString).collect(Collectors.toList())
                : List.of();

        KdsStation station = new KdsStation(storeId, request.name(), request.isExpedition(), sectors);
        KdsStation saved = stationRepository.save(station);
        return KdsStationDtoMapper.toResponse(saved);
    }
}

