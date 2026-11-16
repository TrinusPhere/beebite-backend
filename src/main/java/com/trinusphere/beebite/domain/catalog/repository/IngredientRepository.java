package com.trinusphere.beebite.domain.catalog.repository;

import com.trinusphere.beebite.domain.catalog.entity.Ingredient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngredientRepository {
    Optional<Ingredient> findById(UUID id);

    com.trinusphere.beebite.domain.shared.PageResult<Ingredient> findAllByStoreId(UUID storeId, int page, int size);

    List<Ingredient> findAllByStoreId(UUID storeId); // keeping for N+1 queries cache in Products

    List<Ingredient> findAllById(List<UUID> ids);

    Ingredient save(Ingredient ingredient);

    void deleteById(UUID id);
}
