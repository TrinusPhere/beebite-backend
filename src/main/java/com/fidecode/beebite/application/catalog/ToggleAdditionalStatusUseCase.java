package com.fidecode.beebite.application.catalog;

import com.fidecode.beebite.application.catalog.dto.AdditionalResponse;
import com.fidecode.beebite.domain.catalog.entity.Additional;
import com.fidecode.beebite.domain.catalog.exception.AdditionalNotFoundException;
import com.fidecode.beebite.domain.catalog.repository.AdditionalRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
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
        Additional additional = repository.findById(id)
                .orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        StoreGuard.enforce(additional, storeId);
        additional.toggleActive();
        return AdditionalDtoMapper.toResponse(repository.save(additional));
    }
}
