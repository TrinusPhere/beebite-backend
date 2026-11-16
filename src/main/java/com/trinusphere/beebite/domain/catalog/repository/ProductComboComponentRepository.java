package com.trinusphere.beebite.domain.catalog.repository;

import com.trinusphere.beebite.domain.catalog.entity.ProductComboComponent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductComboComponentRepository {
    ProductComboComponent save(ProductComboComponent component);
    List<ProductComboComponent> saveAll(List<ProductComboComponent> components);
    Optional<ProductComboComponent> findById(UUID id);
    List<ProductComboComponent> findByComboProductId(UUID comboProductId);
    void deleteByComboProductId(UUID comboProductId);
}

