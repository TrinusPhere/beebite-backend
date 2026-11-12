package com.fidecode.beebite.application.kds;

import com.fidecode.beebite.domain.kds.entity.KdsStation;
import com.fidecode.beebite.domain.kds.exception.KdsStationNotFoundException;
import com.fidecode.beebite.domain.kds.repository.KdsStationRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteKdsStationUseCase {

    private final KdsStationRepository stationRepository;

    @Inject
    public DeleteKdsStationUseCase(KdsStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public void execute(UUID storeId, UUID stationId) {
        KdsStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new KdsStationNotFoundException(stationId.toString()));
        StoreGuard.enforce(station, storeId);

        station.markDeleted();
        stationRepository.save(station);
    }
}

