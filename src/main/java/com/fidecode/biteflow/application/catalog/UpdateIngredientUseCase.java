package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.IngredientRequest;
import com.fidecode.biteflow.application.catalog.dto.IngredientResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.exception.IngredientNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.valueobject.Unit;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class UpdateIngredientUseCase {
    private final IngredientRepository repository;

    @Inject
    public UpdateIngredientUseCase(IngredientRepository repository) {
        this.repository = repository;
    }

    public IngredientResponse execute(UUID storeId, UUID id, IngredientRequest request) {
        Ingredient ingredient = repository.findById(id)
                .orElseThrow(() -> new IngredientNotFoundException(id.toString()));
        StoreGuard.enforce(ingredient, storeId);
        ingredient.updateDetails(request.name(), request.unit() != null ? Unit.valueOf(request.unit()) : null,
                request.costPrice(), request.minStock(), request.active());
        return CreateIngredientUseCase.toResponse(repository.save(ingredient));
    }
}
