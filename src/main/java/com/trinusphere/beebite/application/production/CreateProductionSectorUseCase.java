package com.trinusphere.beebite.application.production;

import com.trinusphere.beebite.application.production.dto.*;
import com.trinusphere.beebite.domain.production.entity.ProductionSector;
import com.trinusphere.beebite.domain.production.repository.ProductionSectorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class CreateProductionSectorUseCase {
    private final ProductionSectorRepository repo;

    @Inject
    public CreateProductionSectorUseCase(ProductionSectorRepository repo) {
        this.repo = repo;
    }

    public ProductionSectorResponse execute(UUID storeId, ProductionSectorRequest request) {
        var s = new ProductionSector(storeId, request.name());
        s.configure(request.code(), request.active());
        return ProductionSectorDtoMapper.toResponse(repo.save(s));
    }
}
