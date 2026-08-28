package com.fidecode.biteflow.domain.production.repository;

import com.fidecode.biteflow.domain.production.entity.ProductionSector;
import java.util.*;

public interface ProductionSectorRepository {
    List<ProductionSector> findAllByStoreId(UUID storeId);

    Optional<ProductionSector> findById(UUID id);

    ProductionSector save(ProductionSector sector);

    void deleteById(UUID id);
}
