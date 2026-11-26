package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.IngredientResponse;
import com.trinusphere.beebite.domain.catalog.exception.IngredientNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.IngredientRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
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

    public IngredientResponse execute(UUID storeId, UUID id) {
        var inStore = repository.findByIdInStore(id, storeId);
        var ingredient = (inStore.isPresent() ? inStore : repository.findById(id))
                .orElseThrow(() -> new IngredientNotFoundException(id.toString()));
        StoreGuard.enforce(ingredient, storeId);
        return CreateIngredientUseCase.toResponse(ingredient);
    }
}
