package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Busca produto pelo externalCode — INCLUI soft-deleted.
 * Usado pelo iFood service para resolver conflitos de duplicate key no IMPORT.
 */
@ApplicationScoped
public class GetProductByExternalCodeUseCase {

    private final ProductRepository productRepository;
    private final ProductResponseAssembler productResponseAssembler;

    @Inject
    public GetProductByExternalCodeUseCase(ProductRepository productRepository,
            ProductResponseAssembler productResponseAssembler) {
        this.productRepository = productRepository;
        this.productResponseAssembler = productResponseAssembler;
    }

    /**
     * @return ProductResponse ou null se não encontrado
     */
    public ProductResponse execute(UUID storeId, String externalCode) {
        return productRepository.findByExternalCodeAndStoreId(externalCode, storeId)
                .map(p -> productResponseAssembler.toResponse(p, java.util.Map.of(), null))
                .orElse(null);
    }
}
