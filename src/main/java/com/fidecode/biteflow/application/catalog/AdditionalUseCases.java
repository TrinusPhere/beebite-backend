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

    public AdditionalResponse update(UUID storeId, UUID id, AdditionalRequest request) {
        Additional additional = repository.findById(id)
            .orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        if (!additional.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
        additional.setName(request.name());
        if (request.price() != null) additional.setPrice(request.price());
        additional.setCategory(request.category());
        return toResponse(repository.save(additional));
    }

    public void delete(UUID storeId, UUID id) {
        Additional additional = repository.findById(id).orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        if (!additional.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
        repository.deleteById(id);
    }

    public AdditionalResponse toggleStatus(UUID storeId, UUID id) {
        Additional additional = repository.findById(id)
            .orElseThrow(() -> new AdditionalNotFoundException(id.toString()));
        if (!additional.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
        additional.toggleActive();
        return toResponse(repository.save(additional));
    }

    public com.fidecode.biteflow.application.shared.PageResponse<AdditionalResponse> list(UUID storeId, int page, int size) {
        com.fidecode.biteflow.domain.shared.PageResult<Additional> result = repository.findAllByStoreId(storeId, page, size);
        List<AdditionalResponse> content = result.getContent().stream()
            .map(AdditionalUseCases::toResponse).toList();
        return new com.fidecode.biteflow.application.shared.PageResponse<>(content, result.getPage(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    static AdditionalResponse toResponse(Additional a) {
        return new AdditionalResponse(
            a.getId() != null ? a.getId().toString() : null,
            a.getName(), a.getPrice(), a.getCategory(), a.isActive()
        );
    }
}
