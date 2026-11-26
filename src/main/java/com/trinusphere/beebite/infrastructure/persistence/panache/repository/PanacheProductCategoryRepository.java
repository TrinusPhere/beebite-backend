package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase;
import com.trinusphere.beebite.domain.catalog.entity.ProductCategory;
import com.trinusphere.beebite.domain.catalog.repository.ProductCategoryRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ProductCategoryEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.ProductCategoryMapper;
import com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PanacheProductCategoryRepository implements ProductCategoryRepository {

    @Inject
    ProductCategoryPanacheRepo repo;

    @Inject
    StoreCatalogContextSupport catalogSupport;

    @Inject
    EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;

    @Inject
    EdgeSyncPayloadSerializer payloadSerializer;

    @Override
    public Optional<ProductCategory> findById(UUID id) {
        return repo.findByIdOptional(id).map(ProductCategoryMapper::toDomain);
    }

    @Override
    public Optional<ProductCategory> findByIdInStore(UUID id, UUID storeId) {
        if (id == null) {
            return Optional.empty();
        }
        Optional<ProductCategoryEntity> entityOpt = repo.findByIdOptional(id);
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }
        ProductCategoryEntity entity = entityOpt.get();
        UUID expectedCatalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        UUID contextStore = (expectedCatalogId != null && expectedCatalogId.equals(entity.catalogId))
                ? storeId
                : StoreCatalogContextSupport.FOREIGN_CATALOG_SENTINEL;
        return Optional.of(ProductCategoryMapper.toDomain(entity, contextStore));
    }

    @Override
    public List<ProductCategory> findAllByStoreId(UUID storeId) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        return repo.list("catalogId = ?1 ORDER BY displayOrder ASC, name ASC", catalogId).stream()
                .map(e -> ProductCategoryMapper.toDomain(e, storeId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductCategory save(ProductCategory category) {
        UUID contextStoreId = catalogSupport.isStoreIdKnown(category.getStoreId()) ? category.getStoreId() : null;
        UUID catalogId = contextStoreId != null
                ? catalogSupport.resolveCatalogIdForStore(contextStoreId)
                : catalogSupport.resolveCatalogIdForStore(category.getCatalogId());
        category.assignCatalog(catalogId);

        ProductCategoryEntity existing = category.getId() != null ? repo.findById(category.getId()) : null;
        ProductCategoryEntity entity = ProductCategoryMapper.toEntity(category, catalogId);
        if (existing == null) {
            repo.persist(entity);
        } else {
            entity.createdAt = existing.createdAt;
            entity = repo.getEntityManager().merge(entity);
        }
        repo.getEntityManager().flush();

        ProductCategory saved = ProductCategoryMapper.toDomain(entity, contextStoreId);
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
            ProductCategory deleted = ProductCategoryMapper.toDomain(entity);
            enqueueOutboxIfEdge(deleted, entity.updatedAt, now, null);
        });
    }

    private void enqueueOutboxIfEdge(ProductCategory category, Instant updatedAt, LocalDateTime deletedAt, UUID preferredStoreId) {
        if (enqueueEdgeOutboxUseCase == null || payloadSerializer == null || category.getId() == null) {
            return;
        }
        UUID outboxStoreId = catalogSupport.resolveRepresentativeStoreIdForCatalog(category.getCatalogId(), preferredStoreId);
        if (outboxStoreId != null) {
            enqueueEdgeOutboxUseCase.execute(
                    outboxStoreId,
                    "CATALOG_CATEGORY_UPSERT",
                    category.getId(),
                    payloadSerializer.serializeCatalogCategory(category, updatedAt, deletedAt)
            );
        }
    }
}
