package com.trinusphere.beebite.application.catalog.product;

import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.exception.ProductNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import com.trinusphere.beebite.application.catalog.port.CatalogEventPublisher;
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
