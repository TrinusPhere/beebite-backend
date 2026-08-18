package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.IngredientEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.IngredientMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheIngredientRepository implements IngredientRepository {

    @Inject IngredientPanacheRepo repo;

    @Override
    public Optional<Ingredient> findById(UUID id) {
        return repo.findByIdOptional(id).map(IngredientMapper::toDomain);
    }

    @Override
    public List<Ingredient> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream()
            .map(IngredientMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public com.fidecode.biteflow.domain.shared.PageResult<Ingredient> findAllByStoreId(UUID storeId, int page, int size) {
        var query = repo.find("storeId", storeId).page(io.quarkus.panache.common.Page.of(page, size));
        List<Ingredient> content = query.list().stream().map(IngredientMapper::toDomain).toList();
        return new com.fidecode.biteflow.domain.shared.PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    public List<Ingredient> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return repo.find("id in ?1", ids).list().stream()
            .map(IngredientMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Ingredient save(Ingredient ingredient) {
        IngredientEntity entity = IngredientMapper.toEntity(ingredient);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return IngredientMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            entity.deletedAt = java.time.LocalDateTime.now();
            repo.getEntityManager().merge(entity);
        });
    }
}
