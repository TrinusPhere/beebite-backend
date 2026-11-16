package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GetUserUseCase {

    private final UserRepository userRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    public GetUserUseCase(UserRepository userRepository) {
        this(userRepository, null);
    }

    @Inject
    public GetUserUseCase(UserRepository userRepository, UserStoreAccessRepository userStoreAccessRepository) {
        this.userRepository = userRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
    }

    public UserResponse execute(UUID storeId, UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getStoreId().equals(storeId) && userStoreAccessRepository != null) {
            Optional<UserStoreAccess> accessOpt = userStoreAccessRepository.findByUserAndStore(id, storeId);
            if (accessOpt.isPresent()) {
                UserStoreAccess access = accessOpt.get();
                return new UserResponse(
                        user.getId() != null ? user.getId().toString() : null,
                        user.getName(),
                        user.getLogin(),
                        access.getRole().name(),
                        access.getPermissions().stream().map(Permission::getValue).toList(),
                        user.isActive(),
                        null,
                        storeId.toString()
                );
            }
        }

        StoreGuard.enforce(user, storeId);
        return CreateUserUseCase.toResponse(user);
    }
}
