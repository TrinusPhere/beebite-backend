package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.UpdateUserRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserStoreAccessRepository userStoreAccessRepository;
    private final StoreRepository storeRepository;

    public UpdateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, null, null);
    }

    @Inject
    public UpdateUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserStoreAccessRepository userStoreAccessRepository,
            StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userStoreAccessRepository = userStoreAccessRepository;
        this.storeRepository = storeRepository;
    }

    public UserResponse execute(UUID storeId, UUID id, UpdateUserRequest request) {
        return execute(storeId, null, id, request);
    }

    public UserResponse execute(UUID storeId, Role callerRole, UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        UserGovernanceSupport.ensureCallerCanManageTargetUser(user, callerRole);
        UserGovernanceSupport.ensureUserBelongsToStore(user, storeId, userStoreAccessRepository);

        Role requestedRole = null;
        if (request.role() != null) {
            requestedRole = UserGovernanceSupport.parseAllowedStoreRole(request.role(), callerRole);
        }

        List<UUID> requestedStoreIds = null;
        if (request.storeIds() != null) {
            requestedStoreIds = UserGovernanceSupport.resolveAuthorizedTargetStores(
                    storeId, request.storeIds(), storeRepository, true);
        }

        if (request.name() != null && !request.name().isBlank()) {
            user.rename(request.name().trim());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.changePasswordHash(passwordEncoder.hash(request.password()));
        }
        if (requestedRole != null) {
            user.changeRole(requestedRole);
        }
        if (request.permissions() != null) {
            List<Permission> perms = UserGovernanceSupport.resolvePermissions(user.getRole(), request.permissions(), false);
            user.replacePermissions(perms);
        }

        User saved = userRepository.save(user);
        List<String> effectiveStoreIds = syncStoreAccesses(saved, storeId, requestedStoreIds);

        return new UserResponse(
                saved.getId() != null ? saved.getId().toString() : null,
                saved.getName(),
                saved.getLogin(),
                saved.getRole().name(),
                saved.getPermissions().stream().map(Permission::getValue).toList(),
                saved.isActive(),
                null,
                saved.getStoreId().toString(),
                effectiveStoreIds
        );
    }

    private List<String> syncStoreAccesses(User saved, UUID contextStoreId, List<UUID> requestedStoreIds) {
        if (userStoreAccessRepository == null || saved.getId() == null) {
            return requestedStoreIds != null
                    ? requestedStoreIds.stream().map(UUID::toString).toList()
                    : List.of(saved.getStoreId().toString());
        }

        if (requestedStoreIds != null) {
            Set<UUID> targetSet = new HashSet<>(requestedStoreIds);
            List<UserStoreAccess> existingAccesses = userStoreAccessRepository.findByUserId(saved.getId());
            for (UserStoreAccess existing : existingAccesses) {
                if (!targetSet.contains(existing.getStoreId())) {
                    userStoreAccessRepository.deleteByUserAndStore(saved.getId(), existing.getStoreId());
                }
            }
            for (UUID targetStoreId : requestedStoreIds) {
                upsertAccess(saved, targetStoreId);
            }
            return requestedStoreIds.stream().map(UUID::toString).toList();
        }

        upsertAccess(saved, contextStoreId);
        if (!saved.getStoreId().equals(contextStoreId)) {
            userStoreAccessRepository.findByUserAndStore(saved.getId(), saved.getStoreId())
                    .ifPresent(access -> {
                        access.updateAccess(saved.getRole(), saved.getPermissions());
                        userStoreAccessRepository.save(access);
                    });
        }

        List<UserStoreAccess> currentAccesses = userStoreAccessRepository.findByUserId(saved.getId());
        if (currentAccesses.isEmpty()) {
            return List.of(saved.getStoreId().toString());
        }
        return currentAccesses.stream().map(a -> a.getStoreId().toString()).distinct().toList();
    }

    private void upsertAccess(User saved, UUID targetStoreId) {
        Optional<UserStoreAccess> existingOpt = userStoreAccessRepository.findByUserAndStore(saved.getId(), targetStoreId);
        UserStoreAccess access;
        if (existingOpt.isPresent()) {
            access = existingOpt.get();
            access.updateAccess(saved.getRole(), saved.getPermissions());
        } else {
            access = new UserStoreAccess(saved.getId(), targetStoreId, saved.getRole(), saved.getPermissions());
        }
        userStoreAccessRepository.save(access);
    }
}
