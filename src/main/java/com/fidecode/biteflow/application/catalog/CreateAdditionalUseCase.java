package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.AdditionalRequest;
import com.fidecode.biteflow.application.catalog.dto.AdditionalResponse;
import com.fidecode.biteflow.domain.catalog.entity.Additional;
import com.fidecode.biteflow.domain.catalog.repository.AdditionalRepository;
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
