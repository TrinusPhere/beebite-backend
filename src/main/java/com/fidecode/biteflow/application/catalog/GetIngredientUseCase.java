package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.IngredientResponse;
import com.fidecode.biteflow.domain.catalog.exception.IngredientNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetIngredientUseCase {

    private final IngredientRepository repository;

    @Inject
    public GetIngredientUseCase(IngredientRepository repository) {
        this.repository = repository;
    }

    public IngredientResponse execute(UUID id) {
        return repository.findById(id)
            .map(CreateIngredientUseCase::toResponse)
            .orElseThrow(() -> new IngredientNotFoundException(id.toString()));
    }
}
