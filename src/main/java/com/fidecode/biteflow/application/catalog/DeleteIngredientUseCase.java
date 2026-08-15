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

    public void execute(UUID id) {
        repository.findById(id)
            .orElseThrow(() -> new IngredientNotFoundException(id.toString()));
        repository.deleteById(id);
    }
}
