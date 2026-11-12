package com.fidecode.beebite.application.catalog.product;

import com.fidecode.beebite.domain.catalog.entity.Product;
import com.fidecode.beebite.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.beebite.domain.catalog.repository.ProductRepository;
import com.fidecode.beebite.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.beebite.domain.shared.StoreGuard;
import com.fidecode.beebite.application.catalog.port.CatalogEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteProductUseCase {

    private final ProductRepository productRepository;
    private final CatalogEventPublisher catalogEventPublisher;

    @Inject
    public DeleteProductUseCase(ProductRepository productRepository, CatalogEventPublisher catalogEventPublisher) {
        this.productRepository = productRepository;
        this.catalogEventPublisher = catalogEventPublisher;
    }

    public void execute(UUID storeId, UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id.toString()));
        StoreGuard.enforce(product, storeId);
        productRepository.deleteById(id);
        catalogEventPublisher.publishCatalogChanged(storeId, id, "PRODUCT_DELETED");
    }
}
