package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.CreateStoreForTenantRequest;
import com.trinusphere.beebite.application.platform.dto.OnboardClientResponse;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.platform.exception.TenantNotFoundException;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.UUID;

@ApplicationScoped
public class AddStoreToTenantUseCase {

    private final TenantAccountRepository tenantAccountRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    @Inject
    public AddStoreToTenantUseCase(
            TenantAccountRepository tenantAccountRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository) {
        this.tenantAccountRepository = tenantAccountRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
    }

    public OnboardClientResponse.ProvisionedStoreDto execute(UUID tenantId, CreateStoreForTenantRequest request) {
        tenantAccountRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        Store store = new Store(request.name());
        store.updateInfo(request.name(), null, true, null, request.address());
        String slug = (request.publicSlug() != null && !request.publicSlug().isBlank())
                ? request.publicSlug().trim().toLowerCase()
                : OnboardClientTenantUseCase.slugify(request.name());
        store.definePublicSlug(slug);
        store.assignToTenant(tenantId, request.cnpj());

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
                generatedToken
        );
    }
}

