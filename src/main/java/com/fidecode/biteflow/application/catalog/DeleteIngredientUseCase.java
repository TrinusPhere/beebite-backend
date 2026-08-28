package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.domain.catalog.exception.IngredientNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteIngredientUseCase {

    private final IngredientRepository repository;

    @Inject
    public DeleteIngredientUseCase(IngredientRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID storeId, UUID id) {
        var ingredient = repository.findById(id).orElseThrow(() -> new IngredientNotFoundException(id.toString()));
        StoreGuard.enforce(ingredient, storeId);
        repository.deleteById(id);
    }
}
