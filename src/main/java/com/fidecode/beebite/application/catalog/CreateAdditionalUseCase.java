package com.fidecode.beebite.application.catalog;

import com.fidecode.beebite.application.catalog.dto.AdditionalRequest;
import com.fidecode.beebite.application.catalog.dto.AdditionalResponse;
import com.fidecode.beebite.domain.catalog.entity.Additional;
import com.fidecode.beebite.domain.catalog.repository.AdditionalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class CreateAdditionalUseCase {

    private final AdditionalRepository repository;

    @Inject
    public CreateAdditionalUseCase(AdditionalRepository repository) {
        this.repository = repository;
    }

    public AdditionalResponse execute(UUID storeId, AdditionalRequest request) {
        Additional additional = new Additional(storeId, request.name(), request.price(), request.category());
        return AdditionalDtoMapper.toResponse(repository.save(additional));
    }
}
