package com.trinusphere.beebite.domain.catalog.repository;

import com.trinusphere.beebite.domain.catalog.entity.Ingredient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngredientRepository {
    Optional<Ingredient> findById(UUID id);

    default Optional<Ingredient> findByIdInStore(UUID id, UUID storeId) {
        return findById(id);
    }

    com.trinusphere.beebite.domain.shared.PageResult<Ingredient> findAllByStoreId(UUID storeId, int page, int size);

    List<Ingredient> findAllByStoreId(UUID storeId);

    List<Ingredient> findAllById(List<UUID> ids);

    default List<Ingredient> findAllByIdInStore(List<UUID> ids, UUID storeId) {
        return findAllById(ids);
    }

    Ingredient save(Ingredient ingredient);

    void deleteById(UUID id);
}
