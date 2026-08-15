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

    public List<IngredientResponse> execute(UUID storeId) {
        return repository.findAllByStoreId(storeId).stream()
            .map(CreateIngredientUseCase::toResponse)
            .toList();
    }
}
