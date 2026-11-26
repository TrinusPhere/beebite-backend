package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.CreateStoreForTenantRequest;
import com.trinusphere.beebite.application.platform.dto.OnboardClientResponse;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.catalog.entity.Catalog;
import com.trinusphere.beebite.domain.catalog.exception.CatalogNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.CatalogRepository;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.exception.TenantNotFoundException;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.UUID;

@ApplicationScoped
public class AddStoreToTenantUseCase {

    private final TenantAccountRepository tenantAccountRepository;
    private final CatalogRepository catalogRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    public AddStoreToTenantUseCase(
            TenantAccountRepository tenantAccountRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository) {
        this(tenantAccountRepository, null, storeRepository, userRepository, userStoreAccessRepository);
    }

    @Inject
    public AddStoreToTenantUseCase(
            TenantAccountRepository tenantAccountRepository,
            CatalogRepository catalogRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository) {
        this.tenantAccountRepository = tenantAccountRepository;
        this.catalogRepository = catalogRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
    }

    public OnboardClientResponse.ProvisionedStoreDto execute(UUID tenantId, CreateStoreForTenantRequest request) {
        TenantAccount tenant = tenantAccountRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        Catalog resolvedCatalog = resolveOrCreateCatalog(tenant, request);

        Store store = new Store(request.name());
        store.updateInfo(request.name(), null, true, null, request.address());
        String slug = (request.publicSlug() != null && !request.publicSlug().isBlank())
                ? request.publicSlug().trim().toLowerCase()
                : OnboardClientTenantUseCase.slugify(request.name());
        store.definePublicSlug(slug);
        store.assignToTenant(tenantId, request.cnpj());
        store.assignCatalog(resolvedCatalog.getId());

        String mode = request.syncMode() != null ? request.syncMode().trim().toUpperCase() : "CLOUD";
        String generatedToken = null;
        if ("EDGE".equals(mode)) {
            generatedToken = OnboardClientTenantUseCase.generateEdgeDeviceToken();
            store.configureEdgeSync("EDGE", generatedToken);
        } else {
            store.configureEdgeSync("CLOUD", null);
        }

        Store savedStore = storeRepository.save(store);

        if (request.ownerLoginToLink() != null && !request.ownerLoginToLink().isBlank()) {
            userRepository.findByLogin(request.ownerLoginToLink().trim().toLowerCase())
                    .ifPresent(user -> {
                        if (userStoreAccessRepository.findByUserAndStore(user.getId(), savedStore.getId()).isEmpty()) {
                            userStoreAccessRepository.save(new UserStoreAccess(
                                    user.getId(),
                                    savedStore.getId(),
                                    Role.ADMIN,
                                    Arrays.asList(Permission.values())
                            ));
                        }
                    });
        }

        return new OnboardClientResponse.ProvisionedStoreDto(
                savedStore.getId().toString(),
                savedStore.getName(),
                savedStore.getCnpj(),
                savedStore.getSyncMode(),
                generatedToken,
                resolvedCatalog.getId() != null ? resolvedCatalog.getId().toString() : null,
                resolvedCatalog.getName()
        );
    }

    private Catalog resolveOrCreateCatalog(TenantAccount tenant, CreateStoreForTenantRequest request) {
        if (catalogRepository == null) {
            Catalog fallback = new Catalog(tenant.getId(), "Cardápio Principal - " + tenant.getTradeName(), true);
            fallback.setId(UUID.randomUUID());
            return fallback;
        }

        if (request.newCatalogName() != null && !request.newCatalogName().isBlank()) {
            boolean hasDefault = catalogRepository.findDefaultByTenantId(tenant.getId()).isPresent();
            Catalog created = new Catalog(tenant.getId(), request.newCatalogName().trim(), !hasDefault);
            return catalogRepository.save(created);
        }

        if (request.catalogId() != null && !request.catalogId().isBlank()) {
            UUID targetCatalogId = UUID.fromString(request.catalogId().trim());
            Catalog existing = catalogRepository.findById(targetCatalogId)
                    .orElseThrow(() -> new CatalogNotFoundException(targetCatalogId));
            if (!existing.getTenantId().equals(tenant.getId())) {
                throw new ForbiddenAccessException("O cardápio informado pertence a outro cliente.");
            }
            return existing;
        }

        return catalogRepository.findDefaultByTenantId(tenant.getId())
                .orElseGet(() -> catalogRepository.save(
                        new Catalog(tenant.getId(), "Cardápio Principal - " + tenant.getTradeName(), true)));
    }
}
