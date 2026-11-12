package com.fidecode.beebite.application.inventory;

import com.fidecode.beebite.application.inventory.dto.StockMovementResponse;
import com.fidecode.beebite.domain.catalog.entity.Ingredient;
import com.fidecode.beebite.domain.catalog.repository.IngredientRepository;
import com.fidecode.beebite.domain.inventory.repository.StockMovementRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class ListStockMovementsUseCase {
    private final StockMovementRepository repo;
    private final IngredientRepository ingredients;

    @Inject
    public ListStockMovementsUseCase(StockMovementRepository repo, IngredientRepository ingredients) {
        this.repo = repo;
        this.ingredients = ingredients;
    }

    public List<StockMovementResponse> execute(UUID storeId) {
        var map = new HashMap<UUID, Ingredient>();
        ingredients.findAllByStoreId(storeId).forEach(i -> map.put(i.getId(), i));
        return repo.findAllByStoreId(storeId).stream()
                .map(m -> StockMovementDtoMapper.toResponse(m, map.get(m.getIngredientId()))).toList();
    }
}
