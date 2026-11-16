package com.trinusphere.beebite.application.production;

import com.trinusphere.beebite.domain.production.exception.ProductionSectorNotFoundException;
import com.trinusphere.beebite.domain.production.repository.ProductionSectorRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class DeleteProductionSectorUseCase {
    private final ProductionSectorRepository repo;

    @Inject
    public DeleteProductionSectorUseCase(ProductionSectorRepository repo) {
        this.repo = repo;
    }

    public void execute(UUID storeId, UUID id) {
        var s = repo.findById(id).orElseThrow(() -> new ProductionSectorNotFoundException(id.toString()));
        StoreGuard.enforce(s, storeId);
        repo.deleteById(id);
    }
}
