package com.trinusphere.beebite.application.production;

import com.trinusphere.beebite.application.production.dto.ProductionSectorResponse;
import com.trinusphere.beebite.domain.production.repository.ProductionSectorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class ListProductionSectorsUseCase {
    private final ProductionSectorRepository repo;

    @Inject
    public ListProductionSectorsUseCase(ProductionSectorRepository repo) {
        this.repo = repo;
    }

    public List<ProductionSectorResponse> execute(UUID storeId) {
        return repo.findAllByStoreId(storeId).stream().map(ProductionSectorDtoMapper::toResponse).toList();
    }
}
