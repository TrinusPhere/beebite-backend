package com.fidecode.beebite.application.catalog;

import com.fidecode.beebite.application.catalog.dto.IngredientResponse;
import com.fidecode.beebite.domain.catalog.repository.IngredientRepository;
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

    public com.fidecode.beebite.application.shared.PageResponse<IngredientResponse> execute(UUID storeId, int page,
            int size) {
        com.fidecode.beebite.domain.shared.PageResult<com.fidecode.beebite.domain.catalog.entity.Ingredient> result = repository
                .findAllByStoreId(storeId, page, size);
        List<IngredientResponse> content = result.getContent().stream().map(CreateIngredientUseCase::toResponse)
                .toList();
        return new com.fidecode.beebite.application.shared.PageResponse<>(content, result.getPage(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }
}
