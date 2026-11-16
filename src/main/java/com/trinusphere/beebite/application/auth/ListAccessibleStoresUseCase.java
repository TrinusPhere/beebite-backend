package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.AccessibleStoreResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ListAccessibleStoresUseCase {

    private final UserRepository userRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;
    private final StoreRepository storeRepository;

    @Inject
    public ListAccessibleStoresUseCase(
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository,
            StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
        this.storeRepository = storeRepository;
    }

    public List<AccessibleStoreResponse> execute(UUID userId, UUID activeStoreId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<UserStoreAccess> accesses = userStoreAccessRepository.findByUserId(userId);
        List<AccessibleStoreResponse> result = new ArrayList<>();

        if (accesses.isEmpty()) {
            Optional<Store> primaryStore = storeRepository.findById(user.getStoreId());
            String storeName = primaryStore.map(Store::getName).orElse("Loja Principal");
            String syncMode = primaryStore.map(Store::getSyncMode).orElse("CLOUD");
            var lastSync = primaryStore.map(Store::getLastEdgeSyncAt).orElse(null);

            result.add(new AccessibleStoreResponse(
                    user.getStoreId().toString(),
                    storeName,
                    user.getRole().name(),
                    user.getPermissions().stream().map(Permission::getValue).toList(),
                    syncMode,
                    lastSync,
                    user.getStoreId().equals(activeStoreId)
            ));
            return result;
        }

        for (UserStoreAccess access : accesses) {
            Optional<Store> storeOpt = storeRepository.findById(access.getStoreId());
            String storeName = storeOpt.map(Store::getName).orElse("Loja " + access.getStoreId());
            String syncMode = storeOpt.map(Store::getSyncMode).orElse("CLOUD");
            var lastSync = storeOpt.map(Store::getLastEdgeSyncAt).orElse(null);

            result.add(new AccessibleStoreResponse(
                    access.getStoreId().toString(),
                    storeName,
                    access.getRole().name(),
                    access.getPermissions().stream().map(Permission::getValue).toList(),
                    syncMode,
                    lastSync,
                    access.getStoreId().equals(activeStoreId)
            ));
        }

        return result;
    }
}
