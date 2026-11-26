package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.AdditionalResponse;
import com.trinusphere.beebite.domain.catalog.entity.Additional;
import com.trinusphere.beebite.domain.catalog.exception.AdditionalNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.AdditionalRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class ToggleAdditionalStatusUseCase {

    private final AdditionalRepository repository;

    @Inject
    public ToggleAdditionalStatusUseCase(AdditionalRepository repository) {
        this.repository = repository;
    }

    public AdditionalResponse execute(UUID storeId, UUID id) {
        var inStore = repository.findByIdInStore(id, storeId);
        Additional additional = (inStore.isPresent() ? inStore : repository.findById(id))
                .orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        StoreGuard.enforce(additional, storeId);
        additional.toggleActive();
        return AdditionalDtoMapper.toResponse(repository.save(additional));
    }
}
