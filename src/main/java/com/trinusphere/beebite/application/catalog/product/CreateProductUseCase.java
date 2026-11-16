package com.trinusphere.beebite.application.catalog.product;

import com.trinusphere.beebite.application.catalog.dto.ProductRequest;
import com.trinusphere.beebite.application.catalog.dto.ProductResponse;
import com.trinusphere.beebite.domain.catalog.entity.Ingredient;
import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.entity.TechnicalSheetItem;
import com.trinusphere.beebite.domain.catalog.repository.IngredientRepository;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import com.trinusphere.beebite.domain.catalog.valueobject.ProductType;
import com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus;
import com.trinusphere.beebite.domain.catalog.valueobject.CatalogItemType;
import com.trinusphere.beebite.domain.catalog.valueobject.ChannelPrice;
import com.trinusphere.beebite.domain.catalog.valueobject.Unit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductResponseAssembler productResponseAssembler;

    @Inject
    public CreateProductUseCase(ProductRepository productRepository, IngredientRepository ingredientRepository,
            ProductResponseAssembler productResponseAssembler) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productResponseAssembler = productResponseAssembler;
    }

    public ProductResponse execute(UUID storeId, ProductRequest request) {
        UUID categoryId = request.categoryId() != null && !request.categoryId().isBlank()
                ? UUID.fromString(request.categoryId()) : null;
        Product product = new Product(storeId, request.name(), categoryId, request.price());
        product.updateBasicInfo(product.getName(), product.getCategoryId(), request.description(), product.getPrice(),
                request.estimatedCost(), request.imageUrl());
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
        if (request.productionSectorId() != null && !request.productionSectorId().isBlank())
            product.configureInventory(product.getProductType(), product.isTrackStock(),
                    UUID.fromString(request.productionSectorId()), product.getSaleUnit());
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
                sheet.add(new TechnicalSheetItem(null, ingId, item.quantity(), costSnapshot));
            }
            product.replaceTechnicalSheet(sheet);
        }

        if (request.removableItemIds() != null) {
            product.replaceRemovableItems(request.removableItemIds().stream().map(UUID::fromString).toList());
        }
        if (request.modifierGroupIds() != null) {
            product.replaceModifierGroups(request.modifierGroupIds().stream().map(UUID::fromString).toList());
        }

        Product saved = productRepository.save(product);
        return productResponseAssembler.toResponse(saved, ingredientCache, null);
    }
}
