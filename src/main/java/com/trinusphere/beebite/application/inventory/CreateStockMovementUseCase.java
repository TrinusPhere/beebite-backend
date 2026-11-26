package com.trinusphere.beebite.application.inventory;

import com.trinusphere.beebite.application.inventory.dto.*;
import com.trinusphere.beebite.application.shared.port.TransactionExecutor;
import com.trinusphere.beebite.domain.catalog.exception.IngredientNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.IngredientRepository;
import com.trinusphere.beebite.domain.inventory.entity.StockMovement;
import com.trinusphere.beebite.domain.inventory.exception.InsufficientStockException;
import com.trinusphere.beebite.domain.inventory.repository.StockMovementRepository;
import com.trinusphere.beebite.domain.inventory.valueobject.StockMovementType;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.UUID;

@ApplicationScoped
public class CreateStockMovementUseCase {
    private final StockMovementRepository repo;
    private final IngredientRepository ingredients;
    private final TransactionExecutor tx;

    @Inject
    public CreateStockMovementUseCase(StockMovementRepository repo, IngredientRepository ingredients,
            TransactionExecutor tx) {
        this.repo = repo;
        this.ingredients = ingredients;
        this.tx = tx;
    }

    public StockMovementResponse execute(UUID storeId, StockMovementRequest request) {
        return tx.execute(() -> {
            UUID ingredientId = UUID.fromString(request.ingredientId());
            var inStore = ingredients.findByIdInStore(ingredientId, storeId);
            var ingredient = (inStore.isPresent() ? inStore : ingredients.findById(ingredientId))
                    .orElseThrow(() -> new IngredientNotFoundException(request.ingredientId()));
            StoreGuard.enforce(ingredient, storeId);
            var type = StockMovementType.valueOf(request.type().toUpperCase());
            BigDecimal previous = ingredient.getStock();
            BigDecimal resulting = type.isOutbound() ? previous.subtract(request.quantity())
                    : previous.add(request.quantity());
            if (resulting.signum() < 0)
                throw new InsufficientStockException(ingredient.getName());
            ingredient.adjustStock(resulting);
            ingredients.save(ingredient);
            var movement = StockMovement.create(storeId, ingredientId, type, request.quantity(), previous, resulting,
                    request.reason(), request.document());
            if (request.sourceType() != null && !request.sourceType().isBlank())
                movement.linkSource(request.sourceType(), request.sourceId() != null && !request.sourceId().isBlank()
                        ? UUID.fromString(request.sourceId()) : null);
            return StockMovementDtoMapper.toResponse(repo.save(movement), ingredient);
        });
    }
}
