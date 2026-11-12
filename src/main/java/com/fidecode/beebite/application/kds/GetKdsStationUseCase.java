package com.fidecode.beebite.application.kds;

import com.fidecode.beebite.application.kds.dto.KdsStationDtoMapper;
import com.fidecode.beebite.application.kds.dto.KdsStationResponse;
import com.fidecode.beebite.domain.kds.entity.KdsStation;
import com.fidecode.beebite.domain.kds.exception.KdsStationNotFoundException;
import com.fidecode.beebite.domain.kds.repository.KdsStationRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
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

