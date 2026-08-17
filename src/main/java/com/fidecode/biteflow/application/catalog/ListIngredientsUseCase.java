package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.IngredientResponse;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ListIngredientsUseCase {

    private final IngredientRepository repository;

    @Inject
    public ListIngredientsUseCase(IngredientRepository repository) {
        this.repository = repository;
    }

    public com.fidecode.biteflow.application.shared.PageResponse<IngredientResponse> execute(UUID storeId, int page, int size) {
        com.fidecode.biteflow.domain.shared.PageResult<com.fidecode.biteflow.domain.catalog.entity.Ingredient> result = repository.findAllByStoreId(storeId, page, size);
        List<IngredientResponse> content = result.getContent().stream()
            .map(CreateIngredientUseCase::toResponse)
            .toList();
        return new com.fidecode.biteflow.application.shared.PageResponse<>(content, result.getPage(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
