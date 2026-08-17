package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.domain.catalog.exception.IngredientNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
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
        var ingredient = repository.findById(id)
            .orElseThrow(() -> new IngredientNotFoundException(id.toString()));
        if (!ingredient.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
        repository.deleteById(id);
    }
}
