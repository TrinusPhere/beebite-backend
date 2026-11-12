package com.fidecode.beebite.application.catalog;

import com.fidecode.beebite.domain.catalog.entity.Additional;
import com.fidecode.beebite.domain.catalog.exception.AdditionalNotFoundException;
import com.fidecode.beebite.domain.catalog.repository.AdditionalRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteAdditionalUseCase {

    private final AdditionalRepository repository;

    @Inject
    public DeleteAdditionalUseCase(AdditionalRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID storeId, UUID id) {
        Additional additional = repository.findById(id)
                .orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        StoreGuard.enforce(additional, storeId);
        repository.deleteById(id);
    }
}
