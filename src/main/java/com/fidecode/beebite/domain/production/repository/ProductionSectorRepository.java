package com.fidecode.beebite.domain.production.repository;

import com.fidecode.beebite.domain.production.entity.ProductionSector;
import java.util.*;

public interface ProductionSectorRepository {
    List<ProductionSector> findAllByStoreId(UUID storeId);

    Optional<ProductionSector> findById(UUID id);

    ProductionSector save(ProductionSector sector);

    void deleteById(UUID id);
}
