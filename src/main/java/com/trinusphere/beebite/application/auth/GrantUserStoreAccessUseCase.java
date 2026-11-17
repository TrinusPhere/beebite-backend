package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.AccessibleStoreResponse;
import com.trinusphere.beebite.application.auth.dto.GrantUserStoreAccessRequest;
import com.trinusphere.beebite.application.platform.CanSuperAdminMutateStoreUseCase;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
        return execute(null, null, request);
    }

    public AccessibleStoreResponse execute(UUID callerStoreId, UUID callerUserId, GrantUserStoreAccessRequest request) {
        UUID userId = UUID.fromString(request.userId());
        UUID targetStoreId = UUID.fromString(request.storeId());

        Role callerRole = resolveCallerRole(callerUserId);
        Role role = UserGovernanceSupport.parseAllowedStoreRole(request.role(), callerRole);

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        UserGovernanceSupport.ensureCallerCanManageTargetUser(targetUser, callerRole);

        Store targetStore = storeRepository.findById(targetStoreId)
                .orElseThrow(() -> new StoreNotFoundException(targetStoreId));

        if (callerRole == Role.SUPER_ADMIN
                && !CanSuperAdminMutateStoreUseCase.TRINUSPHERE_TENANT_ID.equals(targetStore.getTenantId())) {
            throw new ForbiddenAccessException(
                    "Ação bloqueada: o Super Administrador não altera acessos de usuários nas lojas dos clientes.");
        }

        validateTenantIsolation(callerStoreId, targetUser, targetStore);

        List<Permission> permissions = UserGovernanceSupport.resolvePermissions(role, request.permissions(), true);

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
                targetStore.getName(),
                saved.getRole().name(),
                saved.getPermissions().stream().map(Permission::getValue).toList(),
                targetStore.getSyncMode(),
                targetStore.getLastEdgeSyncAt(),
                false
        );
    }

    private Role resolveCallerRole(UUID callerUserId) {
        if (callerUserId == null) {
            return null;
        }
        return userRepository.findById(callerUserId)
                .map(User::getRole)
                .orElse(null);
    }

    private void validateTenantIsolation(UUID callerStoreId, User targetUser, Store targetStore) {
        if (callerStoreId != null) {
            Store callerStore = storeRepository.findById(callerStoreId)
                    .orElseThrow(() -> new StoreNotFoundException(callerStoreId));
            UserGovernanceSupport.ensureSameTenantOrStore(callerStore, targetStore,
                    "Acesso negado: a loja de destino pertence a outra empresa.");

            Store userHomeStore = storeRepository.findById(targetUser.getStoreId())
                    .orElseThrow(() -> new StoreNotFoundException(targetUser.getStoreId()));
            UserGovernanceSupport.ensureSameTenantOrStore(callerStore, userHomeStore,
                    "Acesso negado: o usuário informado pertence a outra empresa.");
            return;
        }

        Optional<Store> userHomeStoreOpt = storeRepository.findById(targetUser.getStoreId());
        if (userHomeStoreOpt.isPresent()) {
            Store userHomeStore = userHomeStoreOpt.get();
            if (userHomeStore.getTenantId() != null || targetStore.getTenantId() != null) {
                UserGovernanceSupport.ensureSameTenantOrStore(userHomeStore, targetStore,
                        "Acesso negado: usuário e loja de destino pertencem a empresas diferentes.");
            }
        }
    }
}
