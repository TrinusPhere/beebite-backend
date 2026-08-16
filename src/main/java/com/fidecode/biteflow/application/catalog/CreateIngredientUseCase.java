package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.IngredientRequest;
import com.fidecode.biteflow.application.catalog.dto.IngredientResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.valueobject.Unit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class CreateIngredientUseCase {

    private final IngredientRepository repository;

    @Inject
    public CreateIngredientUseCase(IngredientRepository repository) {
        this.repository = repository;
    }

    public IngredientResponse execute(UUID storeId, IngredientRequest request) {
        Unit unit = request.unit() != null ? Unit.valueOf(request.unit()) : Unit.UN;
        Ingredient ingredient = new Ingredient(storeId, request.name(), request.stock(), unit, request.costPrice(), request.minStock());
        Ingredient saved = repository.save(ingredient);
        return toResponse(saved);
    }

    static IngredientResponse toResponse(Ingredient i) {
        return new IngredientResponse(
            i.getId() != null ? i.getId().toString() : null,
            i.getName(),
            i.getStock(),
            i.getUnit().name(),
            i.getCostPrice(),
            i.getMinStock(),
            i.isLowStock(),
            null
        );
    }
}
