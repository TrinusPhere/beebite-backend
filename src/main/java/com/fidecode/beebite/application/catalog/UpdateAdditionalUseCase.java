package com.fidecode.beebite.application.catalog;

import com.fidecode.beebite.application.catalog.dto.AdditionalRequest;
import com.fidecode.beebite.application.catalog.dto.AdditionalResponse;
import com.fidecode.beebite.domain.catalog.entity.Additional;
import com.fidecode.beebite.domain.catalog.exception.AdditionalNotFoundException;
import com.fidecode.beebite.domain.catalog.repository.AdditionalRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
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
        Additional additional = repository.findById(id)
                .orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        StoreGuard.enforce(additional, storeId);
        additional.update(request.name(), request.price(), request.category());
        return AdditionalDtoMapper.toResponse(repository.save(additional));
    }
}
