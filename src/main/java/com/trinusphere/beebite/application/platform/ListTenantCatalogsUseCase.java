package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.CatalogSummaryDto;
import com.trinusphere.beebite.domain.catalog.entity.Catalog;
import com.trinusphere.beebite.domain.catalog.repository.CatalogRepository;
import com.trinusphere.beebite.domain.platform.exception.TenantNotFoundException;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListTenantCatalogsUseCase {

    private final TenantAccountRepository tenantAccountRepository;
    private final CatalogRepository catalogRepository;
    private final StoreRepository storeRepository;

    @Inject
    public ListTenantCatalogsUseCase(
            TenantAccountRepository tenantAccountRepository,
            CatalogRepository catalogRepository,
            StoreRepository storeRepository) {
        this.tenantAccountRepository = tenantAccountRepository;
        this.catalogRepository = catalogRepository;
        this.storeRepository = storeRepository;
    }

    public List<CatalogSummaryDto> execute(UUID tenantId) {
        tenantAccountRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        List<Catalog> catalogs = catalogRepository.findAllByTenantId(tenantId);
        Map<UUID, Long> storesPerCatalog = storeRepository.findAll().stream()
                .filter(s -> tenantId.equals(s.getTenantId()) && s.getCatalogId() != null)
                .collect(Collectors.groupingBy(Store::getCatalogId, Collectors.counting()));

        return catalogs.stream()
                .map(c -> new CatalogSummaryDto(
                        c.getId().toString(),
                        c.getTenantId().toString(),
                        c.getName(),
                        c.isDefaultCatalog(),
                        storesPerCatalog.getOrDefault(c.getId(), 0L).intValue()
                ))
                .toList();
    }
}

