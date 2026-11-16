package com.trinusphere.beebite.application.catalog.product;

import com.trinusphere.beebite.application.catalog.dto.ProductResponse;
import com.trinusphere.beebite.domain.catalog.entity.*;
import com.trinusphere.beebite.domain.catalog.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductResponseAssembler {
    private final IngredientRepository ingredients;
    private final ProductCategoryRepository categories;

    @Inject
    public ProductResponseAssembler(IngredientRepository ingredients, ProductCategoryRepository categories) {
        this.ingredients = ingredients;
        this.categories = categories;
    }

    public ProductResponse toResponse(Product p) {
        var ids = new ArrayList<UUID>();
        if (p.getTechnicalSheet() != null)
            p.getTechnicalSheet().forEach(i -> ids.add(i.getIngredientId()));
        if (p.getRemovableItemIds() != null)
            ids.addAll(p.getRemovableItemIds());
        var ic = ingredients.findAllById(ids).stream().collect(Collectors.toMap(Ingredient::getId, i -> i));
        Map<UUID, ProductCategory> cc = new HashMap<>();
        if (p.getCategoryId() != null)
            categories.findById(p.getCategoryId()).ifPresent(c -> cc.put(c.getId(), c));
        return ProductDtoMapper.toResponse(p, ic, cc);
    }

    public ProductResponse toResponse(Product p, Map<UUID, Ingredient> ic, Map<UUID, ProductCategory> cc) {
        return ProductDtoMapper.toResponse(p, ic, cc);
    }
}
