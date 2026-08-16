package com.fidecode.biteflow.domain.catalog.repository;

import com.fidecode.biteflow.domain.catalog.entity.Ingredient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngredientRepository {
    Optional<Ingredient> findById(UUID id);
    List<Ingredient> findAllByStoreId(UUID storeId);
    Ingredient save(Ingredient ingredient);
    void deleteById(UUID id);
}
