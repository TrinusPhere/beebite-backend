package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Regras compartilhadas de governança de usuários por loja/tenant (DRY). Centraliza validação de papéis permitidos,
 * isolamento multi-tenant entre lojas e conversão para {@link UserResponse}.
 */
final class UserGovernanceSupport {

    private UserGovernanceSupport() {
    }

    static Role parseAllowedStoreRole(String rawRole, Role callerRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new IllegalArgumentException("role é obrigatório.");
        }
        Role parsed = Role.valueOf(rawRole.trim().toUpperCase());
        if (parsed == Role.SUPER_ADMIN) {
            throw new ForbiddenAccessException("Não é permitido atribuir o perfil SUPER_ADMIN pela gestão de loja.");
        }
        if (callerRole == Role.GERENTE && parsed == Role.ADMIN) {
            throw new ForbiddenAccessException(
                    "Apenas administradores podem criar ou promover usuários para o perfil ADMIN.");
        }
        return parsed;
    }

    static void ensureCallerCanManageTargetUser(User targetUser, Role callerRole) {
        if (targetUser.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenAccessException(
                    "Usuário SUPER_ADMIN não pode ser alterado ou excluído pela gestão de loja.");
        }
        if (callerRole == Role.GERENTE && targetUser.getRole() == Role.ADMIN) {
            throw new ForbiddenAccessException("Apenas administradores podem editar ou excluir usuários ADMIN.");
        }
    }

    static boolean userBelongsToStore(User user, UUID storeId, UserStoreAccessRepository accessRepo) {
        if (user.getStoreId().equals(storeId)) {
            return true;
        }
        if (accessRepo != null && user.getId() != null) {
            return accessRepo.findByUserAndStore(user.getId(), storeId).isPresent();
        }
        return false;
    }

    static void ensureUserBelongsToStore(User user, UUID storeId, UserStoreAccessRepository accessRepo) {
        if (!userBelongsToStore(user, storeId, accessRepo)) {
            StoreGuard.enforce(user, storeId);
        }
    }

    static List<UUID> resolveAuthorizedTargetStores(
            UUID callerStoreId,
            List<String> requestedStoreIds,
            StoreRepository storeRepository,
            boolean requireNonEmptyIfProvided) {
        if (requestedStoreIds == null) {
            return List.of(callerStoreId);
        }

        Set<UUID> deduplicated = new LinkedHashSet<>();
        for (String raw : requestedStoreIds) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            try {
                deduplicated.add(UUID.fromString(raw.trim()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Identificador de loja inválido: " + raw);
            }
        }

        if (deduplicated.isEmpty()) {
            if (requireNonEmptyIfProvided) {
                throw new IllegalArgumentException("O usuário deve estar vinculado a pelo menos uma loja.");
            }
            return List.of(callerStoreId);
        }

        if (storeRepository == null) {
            for (UUID targetId : deduplicated) {
                if (!targetId.equals(callerStoreId)) {
                    throw new ForbiddenAccessException("Acesso negado: loja não autorizada.");
                }
            }
            return List.copyOf(deduplicated);
        }

        boolean needsCrossStoreValidation = deduplicated.stream().anyMatch(id -> !id.equals(callerStoreId));
        Store callerStore = needsCrossStoreValidation
                ? storeRepository.findById(callerStoreId)
                        .orElseThrow(() -> new StoreNotFoundException(callerStoreId))
                : null;

        for (UUID targetId : deduplicated) {
            if (targetId.equals(callerStoreId)) {
                continue;
            }
            Store targetStore = storeRepository.findById(targetId)
                    .orElseThrow(() -> new StoreNotFoundException(targetId));
            ensureSameTenantOrStore(callerStore, targetStore,
                    "Acesso negado: não é permitido vincular usuários a lojas de outra empresa.");
        }

        return new ArrayList<>(deduplicated);
    }

    static void ensureSameTenantOrStore(Store referenceStore, Store targetStore, String errorMessage) {
        if (referenceStore == null || targetStore == null) {
            throw new ForbiddenAccessException(errorMessage);
        }
        if (referenceStore.getId() != null && referenceStore.getId().equals(targetStore.getId())) {
            return;
        }
        if (referenceStore.getTenantId() != null
                && referenceStore.getTenantId().equals(targetStore.getTenantId())) {
            return;
        }
        throw new ForbiddenAccessException(errorMessage);
    }

    static List<Permission> resolvePermissions(Role role, List<String> rawPermissions, boolean defaultAllForAdmin) {
        if (rawPermissions == null) {
            if (defaultAllForAdmin && role == Role.ADMIN) {
                return Arrays.asList(Permission.values());
            }
            return List.of();
        }
        List<Permission> parsed = rawPermissions.stream()
                .filter(p -> p != null && !p.isBlank())
                .map(Permission::fromValue)
                .toList();
        if (parsed.isEmpty() && defaultAllForAdmin && role == Role.ADMIN) {
            return Arrays.asList(Permission.values());
        }
        return parsed;
    }

    static UserResponse toUserResponse(User user, UUID contextStoreId, UserStoreAccessRepository accessRepo) {
        Role effectiveRole = user.getRole();
        List<Permission> effectivePerms = user.getPermissions();
        List<String> storeIds = List.of(user.getStoreId().toString());

        if (accessRepo != null && user.getId() != null) {
            List<UserStoreAccess> accesses = accessRepo.findByUserId(user.getId());
            if (!accesses.isEmpty()) {
                storeIds = accesses.stream()
                        .map(a -> a.getStoreId().toString())
                        .distinct()
                        .toList();
                if (contextStoreId != null) {
                    Optional<UserStoreAccess> contextAccess = accesses.stream()
                            .filter(a -> a.getStoreId().equals(contextStoreId))
                            .findFirst();
                    if (contextAccess.isPresent()) {
                        effectiveRole = contextAccess.get().getRole();
                        effectivePerms = contextAccess.get().getPermissions();
                    }
                }
            }
        }

        return new UserResponse(
                user.getId() != null ? user.getId().toString() : null,
                user.getName(),
                user.getLogin(),
                effectiveRole.name(),
                effectivePerms.stream().map(Permission::getValue).toList(),
                user.isActive(),
                null,
                user.getStoreId().toString(),
                storeIds
        );
    }
}

