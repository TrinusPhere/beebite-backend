package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.AdditionalRequest;
import com.trinusphere.beebite.application.catalog.dto.AdditionalResponse;
import com.trinusphere.beebite.domain.catalog.entity.Additional;
import com.trinusphere.beebite.domain.catalog.exception.AdditionalNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.AdditionalRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateAdditionalUseCase {

    private final AdditionalRepository repository;

    @Inject
    public UpdateAdditionalUseCase(AdditionalRepository repository) {
        this.repository = repository;
    }

    public AdditionalResponse execute(UUID storeId, UUID id, AdditionalRequest request) {
        var inStore = repository.findByIdInStore(id, storeId);
        Additional additional = (inStore.isPresent() ? inStore : repository.findById(id))
                .orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        StoreGuard.enforce(additional, storeId);
        additional.update(request.name(), request.price(), request.category());
        return AdditionalDtoMapper.toResponse(repository.save(additional));
    }
}
