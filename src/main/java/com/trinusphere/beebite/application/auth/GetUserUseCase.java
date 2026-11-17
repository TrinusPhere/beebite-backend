package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;
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

        if (user.getRole() == Role.SUPER_ADMIN) {
            return new UserResponse(
                    user.getId() != null ? user.getId().toString() : null,
                    user.getName(),
                    user.getLogin(),
                    Role.SUPER_ADMIN.name(),
                    Arrays.stream(Permission.values()).map(Permission::getValue).toList(),
                    user.isActive(),
                    null,
                    storeId.toString(),
                    List.of(storeId.toString())
            );
        }

        UserGovernanceSupport.ensureUserBelongsToStore(user, storeId, userStoreAccessRepository);
        return UserGovernanceSupport.toUserResponse(user, storeId, userStoreAccessRepository);
    }
}
