package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.IngredientRequest;
import com.trinusphere.beebite.application.catalog.dto.IngredientResponse;
import com.trinusphere.beebite.domain.catalog.entity.Ingredient;
import com.trinusphere.beebite.domain.catalog.exception.IngredientNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.IngredientRepository;
import com.trinusphere.beebite.domain.catalog.valueobject.Unit;
import com.trinusphere.beebite.domain.shared.StoreGuard;
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
