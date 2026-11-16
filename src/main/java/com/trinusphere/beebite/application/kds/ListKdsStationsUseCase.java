package com.trinusphere.beebite.application.kds;

import com.trinusphere.beebite.application.kds.dto.KdsStationDtoMapper;
import com.trinusphere.beebite.application.kds.dto.KdsStationResponse;
import com.trinusphere.beebite.domain.kds.repository.KdsStationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListKdsStationsUseCase {

    private final KdsStationRepository stationRepository;

    @Inject
    public ListKdsStationsUseCase(KdsStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<KdsStationResponse> execute(UUID storeId) {
        return stationRepository.findActiveByStoreId(storeId).stream()
                .map(KdsStationDtoMapper::toResponse)
                .collect(Collectors.toList());
    }
}

