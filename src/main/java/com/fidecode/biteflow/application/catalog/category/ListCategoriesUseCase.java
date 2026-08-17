package com.fidecode.biteflow.application.catalog.category;

import com.fidecode.biteflow.application.catalog.dto.CategoryResponse;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListCategoriesUseCase {

    private final ProductCategoryRepository categoryRepository;

    @Inject
    public ListCategoriesUseCase(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> execute(UUID storeId) {
        return categoryRepository.findAllByStoreId(storeId).stream()
                .map(CategoryDtoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
