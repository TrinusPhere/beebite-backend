package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductRequest;
import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductType;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductStatus;
import com.fidecode.biteflow.domain.catalog.valueobject.CatalogItemType;
import com.fidecode.biteflow.domain.catalog.valueobject.ChannelPrice;
import com.fidecode.biteflow.domain.catalog.valueobject.Unit;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import com.fidecode.biteflow.application.catalog.port.CatalogEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductResponseAssembler productResponseAssembler;
    private final CatalogEventPublisher catalogEventPublisher;

    @Inject
    public UpdateProductUseCase(ProductRepository productRepository, IngredientRepository ingredientRepository,
            ProductResponseAssembler productResponseAssembler, CatalogEventPublisher catalogEventPublisher) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productResponseAssembler = productResponseAssembler;
        this.catalogEventPublisher = catalogEventPublisher;
    }

    public ProductResponse execute(UUID storeId, UUID id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id.toString()));
        StoreGuard.enforce(product, storeId);

        product.updateBasicInfo(request.name(), product.getCategoryId(), product.getDescription(), product.getPrice(),
                product.getEstimatedCost(), product.getImageUrl());
        UUID categoryId = request.categoryId() != null && !request.categoryId().isBlank()
                ? UUID.fromString(request.categoryId()) : null;
        product.updateBasicInfo(product.getName(), categoryId, product.getDescription(), product.getPrice(),
                product.getEstimatedCost(), product.getImageUrl());
        product.updateBasicInfo(product.getName(), product.getCategoryId(), request.description(), product.getPrice(),
                product.getEstimatedCost(), product.getImageUrl());
        if (request.price() != null)
            product.updateBasicInfo(product.getName(), product.getCategoryId(), product.getDescription(),
                    request.price(), product.getEstimatedCost(), product.getImageUrl());
        if (request.estimatedCost() != null)
            product.updateBasicInfo(product.getName(), product.getCategoryId(), product.getDescription(),
                    product.getPrice(), request.estimatedCost(), product.getImageUrl());
        product.updateBasicInfo(product.getName(), product.getCategoryId(), product.getDescription(),
                product.getPrice(), product.getEstimatedCost(), request.imageUrl());
        if (request.stock() != null)
            product.initializeStock(request.stock());
        if (request.status() != null)
            product.configureAvailability(ProductStatus.valueOf(request.status()), product.isAvailableDigitalMenu(), product.isAvailablePdv());

        product.configureIntegration(request.externalCode(), request.catalogItemType() != null ? CatalogItemType.valueOf(request.catalogItemType()) : null);

        if (request.channelPrices() != null) {
            product.replaceChannelPrices(request.channelPrices().stream()
                    .map(cp -> new ChannelPrice(cp.channel(), cp.price())).toList());
        }
        if (request.productType() != null)
            product.configureInventory(ProductType.valueOf(request.productType()), product.isTrackStock(),
                    product.getProductionSectorId(), product.getSaleUnit());
        if (request.trackStock() != null)
            product.configureInventory(product.getProductType(), request.trackStock(), product.getProductionSectorId(),
                    product.getSaleUnit());
        product.configureInventory(product.getProductType(), product.isTrackStock(),
                request.productionSectorId() != null && !request.productionSectorId().isBlank()
                        ? UUID.fromString(request.productionSectorId()) : null,
                product.getSaleUnit());
        if (request.saleUnit() != null)
            product.configureInventory(product.getProductType(), product.isTrackStock(),
                    product.getProductionSectorId(), Unit.valueOf(request.saleUnit()));
        if (request.availableDigitalMenu() != null)
            product.configureAvailability(product.getStatus(), request.availableDigitalMenu(), product.isAvailablePdv());
        if (request.availablePdv() != null)
            product.configureAvailability(product.getStatus(), product.isAvailableDigitalMenu(), request.availablePdv());

        List<UUID> ingredientIds = new ArrayList<>();
        if (request.technicalSheet() != null) {
            ingredientIds
                    .addAll(request.technicalSheet().stream().map(i -> UUID.fromString(i.ingredientId())).toList());
        }
        if (request.removableItemIds() != null) {
            ingredientIds.addAll(request.removableItemIds().stream().map(UUID::fromString).toList());
        }

        Map<UUID, Ingredient> ingredientCache = ingredientRepository.findAllById(ingredientIds).stream()
                .collect(Collectors.toMap(Ingredient::getId, i -> i));

        if (request.technicalSheet() != null) {
            List<TechnicalSheetItem> sheet = new ArrayList<>();
            for (var item : request.technicalSheet()) {
                UUID ingId = UUID.fromString(item.ingredientId());
                Ingredient ing = ingredientCache.get(ingId);
                BigDecimal costSnapshot = ing != null ? ing.getCostPrice() : BigDecimal.ZERO;
                sheet.add(new TechnicalSheetItem(id, ingId, item.quantity(), costSnapshot));
            }
            product.replaceTechnicalSheet(sheet);
        }

        if (request.removableItemIds() != null) {
            product.replaceRemovableItems(request.removableItemIds().stream().map(UUID::fromString).toList());
        }
        if (request.modifierGroupIds() != null) {
            product.replaceModifierGroups(request.modifierGroupIds().stream().map(UUID::fromString).toList());
        }

        ProductResponse response = productResponseAssembler.toResponse(productRepository.save(product), ingredientCache, null);
        
        catalogEventPublisher.publishCatalogChanged(storeId, id, "PRODUCT_UPDATED");
        
        return response;
    }
}
