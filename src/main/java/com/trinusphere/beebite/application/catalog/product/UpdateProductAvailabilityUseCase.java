package com.trinusphere.beebite.application.catalog.product;

import com.trinusphere.beebite.application.catalog.dto.ProductAvailabilityRequest;
import com.trinusphere.beebite.application.catalog.dto.ProductResponse;
import com.trinusphere.beebite.application.catalog.port.CatalogEventPublisher;
import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.exception.ProductNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UpdateProductAvailabilityUseCase {

    private final ProductRepository productRepository;
    private final ProductResponseAssembler productResponseAssembler;
    private final CatalogEventPublisher catalogEventPublisher;

    @Inject
    public UpdateProductAvailabilityUseCase(ProductRepository productRepository,
            ProductResponseAssembler productResponseAssembler, CatalogEventPublisher catalogEventPublisher) {
        this.productRepository = productRepository;
        this.productResponseAssembler = productResponseAssembler;
        this.catalogEventPublisher = catalogEventPublisher;
    }

    public ProductResponse execute(UUID storeId, UUID productId, ProductAvailabilityRequest request) {
        Product product = productRepository.findByIdInStore(productId, storeId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        StoreGuard.enforce(product, storeId);

        ProductStatus requestedStatus = ProductStatus.valueOf(request.status());

        if (request.isStoreOnly()) {
            // Pausa apenas desta unidade: não altera o Cardápio nem notifica o iFood.
            product.configureLocalStoreStatus(requestedStatus);
            productRepository.updateStoreLocalStatus(storeId, productId, requestedStatus);
            return productResponseAssembler.toResponse(product, Map.of(), null);
        }

        product.configureAvailability(requestedStatus, request.availableDigitalMenu(), request.availablePdv());
        Product saved = productRepository.saveForStore(product, storeId);

        catalogEventPublisher.publishCatalogChanged(
                saved.getCatalogId(),
                storeId,
                saved.getId(),
                "PRODUCT_UPDATED",
                saved.resolveEffectiveIfoodPrice(),
                saved.getGlobalStatus().name());

        return productResponseAssembler.toResponse(saved, Map.of(), null);
    }
}
