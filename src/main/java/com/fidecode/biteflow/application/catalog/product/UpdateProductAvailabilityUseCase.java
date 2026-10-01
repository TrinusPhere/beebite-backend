package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductAvailabilityRequest;
import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductStatus;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateProductAvailabilityUseCase {

    private final ProductRepository productRepository;
    private final ProductResponseAssembler productResponseAssembler;

    @Inject
    public UpdateProductAvailabilityUseCase(ProductRepository productRepository,
            ProductResponseAssembler productResponseAssembler) {
        this.productRepository = productRepository;
        this.productResponseAssembler = productResponseAssembler;
    }

    public ProductResponse execute(UUID storeId, UUID productId, ProductAvailabilityRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        StoreGuard.enforce(product, storeId);

        product.configureAvailability(
                ProductStatus.valueOf(request.status()),
                request.availableDigitalMenu(),
                request.availablePdv()
        );

        Product saved = productRepository.save(product);
        return productResponseAssembler.toResponse(saved, java.util.Map.of(), null);
    }
}
