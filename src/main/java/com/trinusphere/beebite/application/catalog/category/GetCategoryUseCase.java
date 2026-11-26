package com.trinusphere.beebite.application.catalog.category;

import com.trinusphere.beebite.application.catalog.dto.CategoryResponse;
import com.trinusphere.beebite.domain.catalog.exception.CategoryNotFoundException;
import com.trinusphere.beebite.domain.catalog.entity.ProductCategory;
import com.trinusphere.beebite.domain.catalog.repository.ProductCategoryRepository;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetCategoryUseCase {

    private final ProductCategoryRepository categoryRepository;

    @Inject
    public GetCategoryUseCase(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse execute(UUID storeId, UUID id) {
        var inStore = categoryRepository.findByIdInStore(id, storeId);
        ProductCategory category = (inStore.isPresent() ? inStore : categoryRepository.findById(id))
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        StoreGuard.enforce(category, storeId);
        return CategoryDtoMapper.toResponse(category);
    }
}
