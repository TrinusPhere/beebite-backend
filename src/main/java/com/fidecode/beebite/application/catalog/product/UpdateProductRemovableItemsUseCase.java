package com.fidecode.beebite.application.catalog.product;

import com.fidecode.beebite.application.catalog.dto.ProductResponse;
import com.fidecode.beebite.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.beebite.domain.catalog.repository.ProductRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class UpdateProductRemovableItemsUseCase {
    private final ProductRepository repo;
    private final ProductResponseAssembler assembler;

    @Inject
    public UpdateProductRemovableItemsUseCase(ProductRepository repo, ProductResponseAssembler assembler) {
        this.repo = repo;
        this.assembler = assembler;
    }

    public ProductResponse execute(UUID storeId, UUID id, List<String> ingredientIds) {
        var p = repo.findById(id).orElseThrow(() -> new ProductNotFoundException(id.toString()));
        StoreGuard.enforce(p, storeId);
        p.replaceRemovableItems(ingredientIds != null
                ? ingredientIds.stream().filter(x -> x != null && !x.isBlank()).map(UUID::fromString).toList()
                : List.of());
        return assembler.toResponse(repo.save(p));
    }
}
