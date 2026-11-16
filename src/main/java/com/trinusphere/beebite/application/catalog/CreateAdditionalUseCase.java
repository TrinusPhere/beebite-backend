package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.AdditionalRequest;
import com.trinusphere.beebite.application.catalog.dto.AdditionalResponse;
import com.trinusphere.beebite.domain.catalog.entity.Additional;
import com.trinusphere.beebite.domain.catalog.repository.AdditionalRepository;
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
