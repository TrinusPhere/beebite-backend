package com.trinusphere.beebite.application.kds;

import com.trinusphere.beebite.application.kds.dto.KdsStationDtoMapper;
import com.trinusphere.beebite.application.kds.dto.KdsStationResponse;
import com.trinusphere.beebite.domain.kds.entity.KdsStation;
import com.trinusphere.beebite.domain.kds.exception.KdsStationNotFoundException;
import com.trinusphere.beebite.domain.kds.repository.KdsStationRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetKdsStationUseCase {

    private final KdsStationRepository stationRepository;

    @Inject
    public GetKdsStationUseCase(KdsStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public KdsStationResponse execute(UUID storeId, UUID stationId) {
        KdsStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new KdsStationNotFoundException(stationId.toString()));
        StoreGuard.enforce(station, storeId);
        return KdsStationDtoMapper.toResponse(station);
    }
}

