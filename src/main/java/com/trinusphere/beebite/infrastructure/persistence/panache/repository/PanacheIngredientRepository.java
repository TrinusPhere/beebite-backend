package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase;
import com.trinusphere.beebite.domain.catalog.entity.Ingredient;
import com.trinusphere.beebite.domain.catalog.repository.IngredientRepository;
import com.trinusphere.beebite.domain.shared.PageResult;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.IngredientEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.StoreIngredientStockEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.IngredientMapper;
import com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheIngredientRepository implements IngredientRepository {

    @Inject
    IngredientPanacheRepo repo;

    @Inject
    StoreIngredientStockPanacheRepo stockRepo;

    @Inject
    StoreCatalogContextSupport catalogSupport;

    @Inject
    EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;

    @Inject
    EdgeSyncPayloadSerializer payloadSerializer;

    @Override
    public Optional<Ingredient> findById(UUID id) {
        return repo.findByIdOptional(id).map(IngredientMapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Ingredient> findByIdInStore(UUID id, UUID storeId) {
        if (id == null) {
            return Optional.empty();
        }
        Optional<IngredientEntity> entityOpt = repo.findByIdOptional(id);
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }
        IngredientEntity entity = entityOpt.get();
        UUID expectedCatalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        if (expectedCatalogId == null || !expectedCatalogId.equals(entity.catalogId)) {
            return Optional.of(IngredientMapper.toDomain(
                    entity, StoreCatalogContextSupport.FOREIGN_CATALOG_SENTINEL, null));
        }
        StoreIngredientStockEntity stockEntity = stockRepo
                .find("storeId = ?1 and ingredientId = ?2", storeId, id)
                .firstResult();
        return Optional.of(IngredientMapper.toDomain(entity, storeId, stockEntity));
    }

    @Override
    @Transactional
    public List<Ingredient> findAllByStoreId(UUID storeId) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        List<IngredientEntity> entities = repo.find("catalogId", catalogId).list();
        return mapInStoreContext(entities, storeId);
    }

    @Override
    @Transactional
    public PageResult<Ingredient> findAllByStoreId(UUID storeId, int page, int size) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        var query = repo.find("catalogId", catalogId).page(Page.of(page, size));
        List<Ingredient> content = mapInStoreContext(query.list(), storeId);
        return new PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    public List<Ingredient> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        return repo.find("id in ?1", ids).list().stream().map(IngredientMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public List<Ingredient> findAllByIdInStore(List<UUID> ids, UUID storeId) {
        if (ids == null || ids.isEmpty())
            return List.of();
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        List<IngredientEntity> entities = repo.find("id in ?1 and catalogId = ?2", ids, catalogId).list();
        return mapInStoreContext(entities, storeId);
    }

    @Override
    @Transactional
    public Ingredient save(Ingredient ingredient) {
        UUID contextStoreId = catalogSupport.isStoreIdKnown(ingredient.getStoreId()) ? ingredient.getStoreId() : null;
        UUID catalogId = contextStoreId != null
                ? catalogSupport.resolveCatalogIdForStore(contextStoreId)
                : catalogSupport.resolveCatalogIdForStore(ingredient.getCatalogId());
        ingredient.assignCatalog(catalogId);

        IngredientEntity entity = IngredientMapper.toEntity(ingredient, catalogId);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        repo.getEntityManager().flush();

        StoreIngredientStockEntity stockEntity = null;
        if (contextStoreId != null) {
            stockEntity = upsertStoreStock(
                    contextStoreId,
                    entity.id,
                    ingredient.getStock(),
                    ingredient.getMinStock()
            );
        }

        Ingredient saved = IngredientMapper.toDomain(entity, contextStoreId, stockEntity);
        enqueueOutboxIfEdge(saved, entity.updatedAt, null, contextStoreId);
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            LocalDateTime now = LocalDateTime.now();
            entity.deletedAt = now;
            entity.updatedAt = Instant.now();
            repo.getEntityManager().merge(entity);
            Ingredient deleted = IngredientMapper.toDomain(entity);
            enqueueOutboxIfEdge(deleted, entity.updatedAt, now, null);
        });
    }

    private StoreIngredientStockEntity upsertStoreStock(
            UUID storeId, UUID ingredientId, BigDecimal stock, BigDecimal minStock) {
        StoreIngredientStockEntity stockEntity = stockRepo
                .find("storeId = ?1 and ingredientId = ?2", storeId, ingredientId)
                .firstResult();
        if (stockEntity == null) {
            stockEntity = new StoreIngredientStockEntity();
            stockEntity.storeId = storeId;
            stockEntity.ingredientId = ingredientId;
            stockEntity.stock = stock != null ? stock : BigDecimal.ZERO;
            stockEntity.minStock = minStock != null ? minStock : BigDecimal.ZERO;
            stockRepo.persist(stockEntity);
            return stockEntity;
        }
        stockEntity.stock = stock != null ? stock : BigDecimal.ZERO;
        stockEntity.minStock = minStock != null ? minStock : BigDecimal.ZERO;
        return repo.getEntityManager().merge(stockEntity);
    }

    private List<Ingredient> mapInStoreContext(List<IngredientEntity> entities, UUID storeId) {
        if (entities.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = entities.stream().map(e -> e.id).toList();
        Map<UUID, StoreIngredientStockEntity> stocksByIngredient = new HashMap<>();
        if (storeId != null) {
            for (StoreIngredientStockEntity s : stockRepo.find("storeId = ?1 and ingredientId in ?2", storeId, ids).list()) {
                stocksByIngredient.put(s.ingredientId, s);
            }
        }
        return entities.stream()
                .map(e -> IngredientMapper.toDomain(e, storeId, stocksByIngredient.get(e.id)))
                .toList();
    }

    private void enqueueOutboxIfEdge(Ingredient ingredient, Instant updatedAt, LocalDateTime deletedAt, UUID preferredStoreId) {
        if (enqueueEdgeOutboxUseCase == null || payloadSerializer == null || ingredient.getId() == null) {
            return;
        }
        UUID outboxStoreId = catalogSupport.resolveRepresentativeStoreIdForCatalog(ingredient.getCatalogId(), preferredStoreId);
        if (outboxStoreId != null) {
            enqueueEdgeOutboxUseCase.execute(
                    outboxStoreId,
                    "CATALOG_INGREDIENT_UPSERT",
                    ingredient.getId(),
                    payloadSerializer.serializeCatalogIngredient(ingredient, updatedAt, deletedAt)
            );
        }
    }
}
