package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase;
import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus;
import com.trinusphere.beebite.domain.shared.PageResult;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ProductChannelPriceEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ProductEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.StoreProductSettingEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.TechnicalSheetItemEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.ProductMapper;
import com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheProductRepository implements ProductRepository {

    @Inject
    ProductPanacheRepo repo;

    @Inject
    StoreProductSettingPanacheRepo settingRepo;

    @Inject
    StoreCatalogContextSupport catalogSupport;

    @Inject
    EntityManager em;

    @Inject
    EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;

    @Inject
    EdgeSyncPayloadSerializer payloadSerializer;

    @Override
    @Transactional
    public Optional<Product> findById(UUID id) {
        return repo.findByIdOptional(id).map(ProductMapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Product> findByIdInStore(UUID id, UUID storeId) {
        if (id == null) {
            return Optional.empty();
        }
        Optional<ProductEntity> entityOpt = repo.findByIdOptional(id);
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }
        ProductEntity entity = entityOpt.get();
        UUID expectedCatalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        if (expectedCatalogId == null || !expectedCatalogId.equals(entity.catalogId)) {
            return Optional.of(ProductMapper.toDomain(
                    entity, StoreCatalogContextSupport.FOREIGN_CATALOG_SENTINEL, null, null));
        }

        StoreProductSettingEntity setting = settingRepo
                .find("storeId = ?1 and productId = ?2", storeId, id)
                .firstResult();
        Map<String, UUID> sectorsByCode = catalogSupport.loadStoreSectorsByCode(storeId);
        UUID fallbackSectorId = entity.defaultSectorCode != null
                ? sectorsByCode.get(entity.defaultSectorCode.toUpperCase())
                : null;
        return Optional.of(ProductMapper.toDomain(entity, storeId, setting, fallbackSectorId));
    }

    @Override
    @Transactional
    public List<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        List<ProductEntity> entities = repo.find("catalogId = ?1 and categoryId = ?2", catalogId, categoryId).list();
        return mapInStoreContext(entities, storeId);
    }

    @Override
    @Transactional
    public List<Product> findAllByStoreId(UUID storeId) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        List<ProductEntity> entities = repo.find("catalogId", catalogId).list();
        return mapInStoreContext(entities, storeId);
    }

    @Override
    @Transactional
    public PageResult<Product> findAllByStoreId(UUID storeId, int page, int size) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        var query = repo.find("catalogId", catalogId).page(Page.of(page, size));
        List<Product> content = mapInStoreContext(query.list(), storeId);
        return new PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    @Transactional
    public PageResult<Product> findPendingCostByStoreId(UUID storeId, int page, int size) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        var query = repo.find("catalogId = ?1 and estimatedCost is null and technicalSheet is empty", catalogId)
                .page(Page.of(page, size));
        List<Product> content = mapInStoreContext(query.list(), storeId);
        return new PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    @Transactional
    public PageResult<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId, int page, int size) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        var query = repo.find("catalogId = ?1 and categoryId = ?2", catalogId, categoryId)
                .page(Page.of(page, size));
        List<Product> content = mapInStoreContext(query.list(), storeId);
        return new PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    @Transactional
    public List<Product> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        return repo.find("id in ?1", ids).list().stream().map(ProductMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public List<Product> findAllByIdInStore(List<UUID> ids, UUID storeId) {
        if (ids == null || ids.isEmpty())
            return List.of();
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        List<ProductEntity> entities = repo.find("id in ?1 and catalogId = ?2", ids, catalogId).list();
        return mapInStoreContext(entities, storeId);
    }

    @Override
    @Transactional
    public Product save(Product product) {
        return saveForStore(product, product.getStoreId());
    }

    @Override
    @Transactional
    public Product saveForStore(Product product, UUID storeId) {
        UUID targetStoreId = (storeId != null && catalogSupport.isStoreIdKnown(storeId))
                ? storeId
                : (catalogSupport.isStoreIdKnown(product.getStoreId()) ? product.getStoreId() : null);
        UUID catalogId = targetStoreId != null
                ? catalogSupport.resolveCatalogIdForStore(targetStoreId)
                : catalogSupport.resolveCatalogIdForStore(product.getCatalogId());

        String sectorCode = product.getProductionSectorId() != null
                ? catalogSupport.resolveSectorCode(product.getProductionSectorId())
                : (product.getDefaultSectorCode() != null ? product.getDefaultSectorCode() : "KITCHEN");
        product.defineDefaultSectorCode(sectorCode);
        product.assignCatalog(catalogId);

        ProductEntity entity;
        if (product.getId() == null) {
            entity = ProductMapper.toEntity(product, catalogId);
            repo.persist(entity);
            em.flush();
        } else {
            entity = repo.findById(product.getId());
            if (entity == null) {
                ProductEntity newEntity = ProductMapper.toEntity(product, catalogId);
                entity = em.merge(newEntity);
                em.flush();
            } else {
                entity.catalogId = catalogId;
                entity.name = product.getName();
                entity.categoryId = product.getCategoryId();
                entity.description = product.getDescription();
                entity.price = product.getPrice();
                entity.estimatedCost = product.getEstimatedCost();
                entity.imageUrl = product.getImageUrl();
                entity.status = product.getGlobalStatus().name();
                entity.externalCode = product.getExternalCode();
                entity.catalogItemType = product.getCatalogItemType().name();
                entity.productType = product.getProductType().name();
                entity.trackStock = product.isTrackStock();
                entity.defaultSectorCode = sectorCode;
                entity.saleUnit = product.getSaleUnit().name();
                entity.availableDigitalMenu = product.isAvailableDigitalMenu();
                entity.availablePdv = product.isAvailablePdv();

                entity.removableItemIds.clear();
                if (product.getRemovableItemIds() != null) {
                    entity.removableItemIds.addAll(product.getRemovableItemIds());
                }

                entity.modifierGroupIds.clear();
                if (product.getModifierGroupIds() != null) {
                    entity.modifierGroupIds.addAll(product.getModifierGroupIds());
                }

                entity.channelPrices.clear();
                if (product.getChannelPrices() != null) {
                    for (var cp : product.getChannelPrices()) {
                        ProductChannelPriceEntity cpe = new ProductChannelPriceEntity();
                        cpe.product = entity;
                        cpe.channel = cp.getChannel();
                        cpe.price = cp.getPrice();
                        entity.channelPrices.add(cpe);
                    }
                }

                if (product.getTechnicalSheet() == null) {
                    entity.technicalSheet.clear();
                } else {
                    entity.technicalSheet.removeIf(tse -> product.getTechnicalSheet().stream()
                            .noneMatch(i -> i.getIngredientId().equals(tse.ingredientId)));
                    for (var item : product.getTechnicalSheet()) {
                        var existing = entity.technicalSheet.stream()
                                .filter(tse -> tse.ingredientId.equals(item.getIngredientId())).findFirst();
                        if (existing.isPresent()) {
                            existing.get().quantity = item.getQuantity();
                            existing.get().costSnapshot = item.getCostSnapshot();
                        } else {
                            var tse = new TechnicalSheetItemEntity();
                            tse.id = item.getId();
                            tse.product = entity;
                            tse.ingredientId = item.getIngredientId();
                            tse.quantity = item.getQuantity();
                            tse.costSnapshot = item.getCostSnapshot();
                            entity.technicalSheet.add(tse);
                        }
                    }
                }

                entity = em.merge(entity);
                em.flush();
            }
        }

        StoreProductSettingEntity setting = null;
        UUID fallbackSectorId = null;
        if (targetStoreId != null) {
            setting = upsertStoreSetting(
                    targetStoreId,
                    entity.id,
                    product.getStock(),
                    product.getLocalStatus() != null ? product.getLocalStatus() : ProductStatus.AVAILABLE,
                    product.getProductionSectorId()
            );
            Map<String, UUID> sectorsByCode = catalogSupport.loadStoreSectorsByCode(targetStoreId);
            fallbackSectorId = entity.defaultSectorCode != null
                    ? sectorsByCode.get(entity.defaultSectorCode.toUpperCase())
                    : null;
        }

        Product saved = ProductMapper.toDomain(entity, targetStoreId, setting, fallbackSectorId);
        enqueueProductOutboxIfEdge(saved, entity.updatedAt, null, targetStoreId);
        return saved;
    }

    @Override
    @Transactional
    public void updateStoreLocalStatus(UUID storeId, UUID productId, ProductStatus localStatus) {
        if (storeId == null || productId == null) {
            return;
        }
        StoreProductSettingEntity setting = settingRepo
                .find("storeId = ?1 and productId = ?2", storeId, productId)
                .firstResult();
        if (setting == null) {
            setting = new StoreProductSettingEntity();
            setting.storeId = storeId;
            setting.productId = productId;
            setting.stock = 0;
            setting.localStatus = (localStatus != null ? localStatus : ProductStatus.AVAILABLE).name();
            settingRepo.persist(setting);
        } else {
            setting.localStatus = (localStatus != null ? localStatus : ProductStatus.AVAILABLE).name();
            em.merge(setting);
        }
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            LocalDateTime now = LocalDateTime.now();
            entity.deletedAt = now;
            entity.updatedAt = Instant.now();
            em.merge(entity);
            Product deletedDomain = ProductMapper.toDomain(entity);
            UUID outboxStoreId = catalogSupport.resolveRepresentativeStoreIdForCatalog(entity.catalogId, null);
            enqueueProductOutboxIfEdge(deletedDomain, entity.updatedAt, now, outboxStoreId);
        });
    }

    @Override
    @Transactional
    public Optional<Product> findByExternalCodeAndStoreId(String externalCode, UUID storeId) {
        UUID catalogId = catalogSupport.resolveCatalogIdForStore(storeId);
        return findByExternalCodeInternal(externalCode, catalogId, storeId);
    }

    @Override
    @Transactional
    public Optional<Product> findByExternalCodeAndCatalogId(String externalCode, UUID catalogId) {
        return findByExternalCodeInternal(externalCode, catalogId, null);
    }

    @SuppressWarnings("unchecked")
    private Optional<Product> findByExternalCodeInternal(String externalCode, UUID catalogId, UUID contextStoreId) {
        if (externalCode == null || externalCode.isBlank() || catalogId == null) {
            return Optional.empty();
        }
        var results = (List<Object[]>) em.createNativeQuery(
                        "SELECT id::text, deleted_at FROM product WHERE external_code = :code AND catalog_id = :catalogId LIMIT 1")
                .setParameter("code", externalCode)
                .setParameter("catalogId", catalogId)
                .getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        Object[] row = results.get(0);
        UUID productId = UUID.fromString(row[0].toString());
        boolean isSoftDeleted = row[1] != null;

        if (isSoftDeleted) {
            em.createNativeQuery("UPDATE product SET deleted_at = NULL, updated_at = now() WHERE id = :id")
                    .setParameter("id", productId)
                    .executeUpdate();
            em.flush();
            em.clear();
        }

        if (contextStoreId != null) {
            return findByIdInStore(productId, contextStoreId);
        }
        ProductEntity entity = em.find(ProductEntity.class, productId);
        return Optional.ofNullable(entity).map(ProductMapper::toDomain);
    }

    private StoreProductSettingEntity upsertStoreSetting(
            UUID storeId, UUID productId, int stock, ProductStatus localStatus, UUID productionSectorId) {
        StoreProductSettingEntity setting = settingRepo
                .find("storeId = ?1 and productId = ?2", storeId, productId)
                .firstResult();
        if (setting == null) {
            setting = new StoreProductSettingEntity();
            setting.storeId = storeId;
            setting.productId = productId;
            setting.stock = Math.max(0, stock);
            setting.localStatus = localStatus != null ? localStatus.name() : "AVAILABLE";
            setting.productionSectorId = productionSectorId;
            settingRepo.persist(setting);
            return setting;
        }
        setting.stock = Math.max(0, stock);
        if (localStatus != null) {
            setting.localStatus = localStatus.name();
        }
        setting.productionSectorId = productionSectorId;
        return em.merge(setting);
    }

    private List<Product> mapInStoreContext(List<ProductEntity> entities, UUID storeId) {
        if (entities.isEmpty()) {
            return List.of();
        }
        List<UUID> productIds = entities.stream().map(e -> e.id).toList();
        Map<UUID, StoreProductSettingEntity> settingsByProduct = new HashMap<>();
        if (storeId != null) {
            for (StoreProductSettingEntity s : settingRepo.find("storeId = ?1 and productId in ?2", storeId, productIds).list()) {
                settingsByProduct.put(s.productId, s);
            }
        }
        Map<String, UUID> sectorsByCode = catalogSupport.loadStoreSectorsByCode(storeId);

        return entities.stream().map(e -> {
            StoreProductSettingEntity setting = settingsByProduct.get(e.id);
            UUID fallbackSectorId = e.defaultSectorCode != null
                    ? sectorsByCode.get(e.defaultSectorCode.toUpperCase())
                    : null;
            return ProductMapper.toDomain(e, storeId, setting, fallbackSectorId);
        }).toList();
    }

    private void enqueueProductOutboxIfEdge(Product product, Instant updatedAt, LocalDateTime deletedAt, UUID preferredStoreId) {
        if (enqueueEdgeOutboxUseCase == null || payloadSerializer == null || product.getId() == null) {
            return;
        }
        UUID outboxStoreId = catalogSupport.resolveRepresentativeStoreIdForCatalog(product.getCatalogId(), preferredStoreId);
        if (outboxStoreId != null) {
            enqueueEdgeOutboxUseCase.execute(
                    outboxStoreId,
                    "CATALOG_PRODUCT_UPSERT",
                    product.getId(),
                    payloadSerializer.serializeCatalogProduct(product, updatedAt, deletedAt)
            );
        }
    }
}
