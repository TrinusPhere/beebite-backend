package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.AccessibleStoreResponse;
import com.trinusphere.beebite.application.auth.dto.GrantUserStoreAccessRequest;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GrantUserStoreAccessUseCase {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    @Inject
    public GrantUserStoreAccessUseCase(
            UserRepository userRepository,
            StoreRepository storeRepository,
            UserStoreAccessRepository userStoreAccessRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
    }

    public AccessibleStoreResponse execute(GrantUserStoreAccessRequest request) {
        UUID userId = UUID.fromString(request.userId());
        UUID targetStoreId = UUID.fromString(request.storeId());

        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Store store = storeRepository.findById(targetStoreId)
                .orElseThrow(() -> new StoreNotFoundException(targetStoreId));

        Role role = Role.valueOf(request.role().trim().toUpperCase());
        List<Permission> permissions;
        if (request.permissions() != null && !request.permissions().isEmpty()) {
            permissions = request.permissions().stream().map(Permission::fromValue).toList();
        } else if (role == Role.ADMIN) {
            permissions = Arrays.asList(Permission.values());
        } else {
            permissions = List.of();
        }

        Optional<UserStoreAccess> existingOpt = userStoreAccessRepository.findByUserAndStore(userId, targetStoreId);
        UserStoreAccess access;
        if (existingOpt.isPresent()) {
            access = existingOpt.get();
            access.updateAccess(role, permissions);
        } else {
            access = new UserStoreAccess(userId, targetStoreId, role, permissions);
        }

        UserStoreAccess saved = userStoreAccessRepository.save(access);

        return new AccessibleStoreResponse(
                saved.getStoreId().toString(),
                store.getName(),
                saved.getRole().name(),
                saved.getPermissions().stream().map(Permission::getValue).toList(),
                store.getSyncMode(),
                store.getLastEdgeSyncAt(),
                false
        );
    }
}
