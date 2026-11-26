package com.trinusphere.beebite.application.catalog.category;

import com.trinusphere.beebite.application.catalog.dto.CategoryRequest;
import com.trinusphere.beebite.application.catalog.dto.CategoryResponse;
import com.trinusphere.beebite.domain.catalog.exception.CategoryNotFoundException;
import com.trinusphere.beebite.domain.catalog.entity.ProductCategory;
import com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus;
import com.trinusphere.beebite.domain.catalog.repository.ProductCategoryRepository;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UpdateCategoryUseCase {

    private final ProductCategoryRepository categoryRepository;

    @Inject
    public UpdateCategoryUseCase(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse execute(UUID storeId, UUID id, CategoryRequest request) {
        var inStore = categoryRepository.findByIdInStore(id, storeId);
        ProductCategory category = (inStore.isPresent() ? inStore : categoryRepository.findById(id))
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        StoreGuard.enforce(category, storeId);

        List<UUID> modifierGroupIds = null;
        if (request.defaultModifierGroupIds() != null) {
            modifierGroupIds = request.defaultModifierGroupIds().stream().map(UUID::fromString)
                    .collect(Collectors.toList());
        }

        category.update(request.name(), request.status() != null ? ProductStatus.valueOf(request.status()) : category.getStatus(),
                request.displayOrder() != null ? request.displayOrder() : category.getDisplayOrder(), modifierGroupIds);

        return CategoryDtoMapper.toResponse(categoryRepository.save(category));
    }
}
