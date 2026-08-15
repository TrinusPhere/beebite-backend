package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.AdditionalRequest;
import com.fidecode.biteflow.application.catalog.dto.AdditionalResponse;
import com.fidecode.biteflow.domain.catalog.entity.Additional;
import com.fidecode.biteflow.domain.catalog.exception.AdditionalNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.AdditionalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AdditionalUseCases {

    private final AdditionalRepository repository;

    @Inject
    public AdditionalUseCases(AdditionalRepository repository) {
        this.repository = repository;
    }

    public AdditionalResponse create(UUID storeId, AdditionalRequest request) {
        Additional additional = new Additional(storeId, request.name(), request.price(), request.category());
        return toResponse(repository.save(additional));
    }

    public AdditionalResponse update(UUID id, AdditionalRequest request) {
        Additional additional = repository.findById(id)
            .orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        additional.setName(request.name());
        if (request.price() != null) additional.setPrice(request.price());
        additional.setCategory(request.category());
        return toResponse(repository.save(additional));
    }

    public void delete(UUID id) {
        repository.findById(id).orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        repository.deleteById(id);
    }

    public AdditionalResponse toggleStatus(UUID id) {
        Additional additional = repository.findById(id)
            .orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        additional.toggleActive();
        return toResponse(repository.save(additional));
    }

    public List<AdditionalResponse> list(UUID storeId) {
        return repository.findAllByStoreId(storeId).stream().map(AdditionalUseCases::toResponse).toList();
    }

    static AdditionalResponse toResponse(Additional a) {
        return new AdditionalResponse(
            a.getId() != null ? a.getId().toString() : null,
            a.getName(), a.getPrice(), a.getCategory(), a.isActive()
        );
    }
}
