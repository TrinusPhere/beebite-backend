package com.fidecode.biteflow.application.financial;

import com.fidecode.biteflow.application.financial.dto.ProductCmvResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.financial.valueobject.CmvAlertLevel;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use Case — Calcula CMV unitário de um produto via ficha técnica. productionCost = sum(costSnapshot × quantity) para
 * cada ingrediente CMV% = (productionCost / sellingPrice) * 100
 */
@ApplicationScoped
public class CalculateProductCmvUseCase {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;

    @Inject
    public CalculateProductCmvUseCase(ProductRepository productRepository, IngredientRepository ingredientRepository) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public ProductCmvResponse execute(UUID storeId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        StoreGuard.enforce(product, storeId);

        BigDecimal productionCost = product.calculateTotalCost();
        BigDecimal sellingPrice = product.getPrice();
        BigDecimal contributionMargin = sellingPrice.subtract(productionCost);

        BigDecimal cmvPercentage = BigDecimal.ZERO;
        if (sellingPrice.compareTo(BigDecimal.ZERO) > 0) {
            cmvPercentage = productionCost.divide(sellingPrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        String alertLevel = CmvAlertLevel.fromPercentage(cmvPercentage).name();

        // Busca ingredientes para enriquecer com nome e unidade
        List<UUID> ingredientIds = product.getTechnicalSheet().stream().map(TechnicalSheetItem::getIngredientId)
                .toList();

        Map<UUID, Ingredient> ingredientMap = ingredientRepository.findAllById(ingredientIds).stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));

        List<ProductCmvResponse.IngredientCost> ingredientCosts = product.getTechnicalSheet().stream().map(item -> {
            Ingredient ingredient = ingredientMap.get(item.getIngredientId());
            String name = ingredient != null ? ingredient.getName() : "Ingrediente desconhecido";
            String unit = ingredient != null ? ingredient.getUnit().name() : "UN";
            return new ProductCmvResponse.IngredientCost(name, item.getQuantity(), unit, item.getLineCost());
        }).toList();

        return new ProductCmvResponse(product.getId(), product.getName(), sellingPrice, productionCost,
                contributionMargin, cmvPercentage, alertLevel, ingredientCosts);
    }
}
