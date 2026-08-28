package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class UpdateProductModifierGroupsUseCase {
    private final ProductRepository repo;
    private final ProductResponseAssembler assembler;

    @Inject
    public UpdateProductModifierGroupsUseCase(ProductRepository repo, ProductResponseAssembler assembler) {
        this.repo = repo;
        this.assembler = assembler;
    }

    public ProductResponse execute(UUID storeId, UUID id, List<String> groupIds) {
        var p = repo.findById(id).orElseThrow(() -> new ProductNotFoundException(id.toString()));
        StoreGuard.enforce(p, storeId);
        p.replaceModifierGroups(groupIds != null
                ? groupIds.stream().filter(x -> x != null && !x.isBlank()).map(UUID::fromString).toList() : List.of());
        return assembler.toResponse(repo.save(p));
    }
}
