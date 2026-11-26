package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.ProductComboComponentDtoMapper;
import com.trinusphere.beebite.application.catalog.dto.ProductComboComponentResponse;
import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.entity.ProductComboComponent;
import com.trinusphere.beebite.domain.catalog.exception.ProductNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.ProductComboComponentRepository;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListComboComponentsUseCase {

    private final ProductRepository productRepository;
    private final ProductComboComponentRepository comboComponentRepository;

    @Inject
    public ListComboComponentsUseCase(ProductRepository productRepository,
                                      ProductComboComponentRepository comboComponentRepository) {
        this.productRepository = productRepository;
        this.comboComponentRepository = comboComponentRepository;
    }

    public List<ProductComboComponentResponse> execute(UUID storeId, UUID comboProductId) {
        var inStore = productRepository.findByIdInStore(comboProductId, storeId);
        Product comboProduct = (inStore.isPresent() ? inStore : productRepository.findById(comboProductId))
                .orElseThrow(() -> new ProductNotFoundException(comboProductId.toString()));
        StoreGuard.enforce(comboProduct, storeId);

        List<ProductComboComponent> components = comboComponentRepository.findByComboProductId(comboProductId);
        if (components.isEmpty()) {
            return List.of();
        }

        List<UUID> compProductIds = components.stream()
                .map(ProductComboComponent::getComponentProductId)
                .collect(Collectors.toList());

        Map<UUID, Product> compMap = productRepository.findAllById(compProductIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return components.stream()
                .map(c -> {
                    Product p = compMap.get(c.getComponentProductId());
                    String name = p != null ? p.getName() : "Item do Combo";
                    return ProductComboComponentDtoMapper.toResponse(c, name);
                })
                .collect(Collectors.toList());
    }
}

