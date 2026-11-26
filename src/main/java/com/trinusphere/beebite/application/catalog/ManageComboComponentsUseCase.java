package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.ProductComboComponentDtoMapper;
import com.trinusphere.beebite.application.catalog.dto.ProductComboComponentRequest;
import com.trinusphere.beebite.application.catalog.dto.ProductComboComponentResponse;
import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.entity.ProductComboComponent;
import com.trinusphere.beebite.domain.catalog.exception.ProductNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.ProductComboComponentRepository;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ManageComboComponentsUseCase {

    private final ProductRepository productRepository;
    private final ProductComboComponentRepository comboComponentRepository;

    @Inject
    public ManageComboComponentsUseCase(ProductRepository productRepository,
                                        ProductComboComponentRepository comboComponentRepository) {
        this.productRepository = productRepository;
        this.comboComponentRepository = comboComponentRepository;
    }

    public List<ProductComboComponentResponse> execute(UUID storeId, UUID comboProductId,
                                                       List<ProductComboComponentRequest> requests) {
        var inStore = productRepository.findByIdInStore(comboProductId, storeId);
        Product comboProduct = (inStore.isPresent() ? inStore : productRepository.findById(comboProductId))
                .orElseThrow(() -> new ProductNotFoundException(comboProductId.toString()));
        StoreGuard.enforce(comboProduct, storeId);

        comboComponentRepository.deleteByComboProductId(comboProductId);

        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<ProductComboComponent> newComponents = new ArrayList<>();
        List<UUID> compProductIds = new ArrayList<>();

        for (ProductComboComponentRequest req : requests) {
            UUID compId = UUID.fromString(req.componentProductId());
            compProductIds.add(compId);
            newComponents.add(new ProductComboComponent(storeId, comboProductId, compId, req.quantity()));
        }

        Map<UUID, Product> compMap = productRepository.findAllById(compProductIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<ProductComboComponent> saved = comboComponentRepository.saveAll(newComponents);

        return saved.stream()
                .map(c -> {
                    Product p = compMap.get(c.getComponentProductId());
                    String name = p != null ? p.getName() : "Item do Combo";
                    return ProductComboComponentDtoMapper.toResponse(c, name);
                })
                .collect(Collectors.toList());
    }
}

