package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.CreateUserRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserAlreadyExistsException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserStoreAccessRepository userStoreAccessRepository;
    private final StoreRepository storeRepository;

    public CreateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, null, null);
    }

    public CreateUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserStoreAccessRepository userStoreAccessRepository) {
        this(userRepository, passwordEncoder, userStoreAccessRepository, null);
    }

    @Inject
    public CreateUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserStoreAccessRepository userStoreAccessRepository,
            StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userStoreAccessRepository = userStoreAccessRepository;
        this.storeRepository = storeRepository;
    }

    public UserResponse execute(UUID storeId, CreateUserRequest request) {
        return execute(storeId, null, request);
    }

    public UserResponse execute(UUID storeId, Role callerRole, CreateUserRequest request) {
        Role role = UserGovernanceSupport.parseAllowedStoreRole(request.role(), callerRole);
        List<UUID> targetStores = UserGovernanceSupport.resolveAuthorizedTargetStores(
                storeId, request.storeIds(), storeRepository, false);

        String cleanLogin = request.login().trim();
        if (userRepository.existsByLogin(cleanLogin)) {
            throw new UserAlreadyExistsException(cleanLogin);
        }

        List<Permission> permissions = UserGovernanceSupport.resolvePermissions(role, request.permissions(), false);
        UUID homeStoreId = targetStores.contains(storeId) ? storeId : targetStores.get(0);

        String hashed = passwordEncoder.hash(request.password());
        User user = new User(homeStoreId, request.name().trim(), cleanLogin, hashed, role, permissions);
        User saved = userRepository.save(user);

        if (userStoreAccessRepository != null && saved.getId() != null) {
            for (UUID targetStoreId : targetStores) {
                userStoreAccessRepository.save(new UserStoreAccess(saved.getId(), targetStoreId, role, permissions));
            }
        }

        List<String> storeIds = targetStores.stream().map(UUID::toString).toList();
        return new UserResponse(
                saved.getId() != null ? saved.getId().toString() : null,
                saved.getName(),
                saved.getLogin(),
                saved.getRole().name(),
                saved.getPermissions().stream().map(Permission::getValue).toList(),
                saved.isActive(),
                null,
                saved.getStoreId().toString(),
                storeIds
        );
    }

    static UserResponse toResponse(User u) {
        return UserGovernanceSupport.toUserResponse(u, null, null);
    }
}
