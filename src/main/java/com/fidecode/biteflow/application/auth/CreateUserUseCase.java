package com.fidecode.biteflow.application.auth;

import com.fidecode.biteflow.application.auth.dto.CreateUserRequest;
import com.fidecode.biteflow.application.auth.dto.UserResponse;
import com.fidecode.biteflow.application.auth.port.PasswordEncoder;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.entity.UserStoreAccess;
import com.fidecode.biteflow.domain.auth.exception.UserAlreadyExistsException;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.domain.auth.repository.UserStoreAccessRepository;
import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserStoreAccessRepository userStoreAccessRepository;

    public CreateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, null);
    }

    @Inject
    public CreateUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserStoreAccessRepository userStoreAccessRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userStoreAccessRepository = userStoreAccessRepository;
    }

    public UserResponse execute(UUID storeId, CreateUserRequest request) {
        if (userRepository.existsByLogin(request.login())) {
            throw new UserAlreadyExistsException(request.login());
        }

        Role role = Role.valueOf(request.role());
        List<Permission> permissions = request.permissions() != null
                ? request.permissions().stream().map(Permission::fromValue).toList() : List.of();

        String hashed = passwordEncoder.hash(request.password());
        User user = new User(storeId, request.name(), request.login(), hashed, role, permissions);
        User saved = userRepository.save(user);

        if (userStoreAccessRepository != null && saved.getId() != null) {
            userStoreAccessRepository.save(new UserStoreAccess(saved.getId(), storeId, role, permissions));
        }

        return toResponse(saved);
    }

    static UserResponse toResponse(User u) {
        return new UserResponse(u.getId() != null ? u.getId().toString() : null, u.getName(), u.getLogin(),
                u.getRole().name(), u.getPermissions().stream().map(Permission::getValue).toList(), u.isActive(), null, u.getStoreId().toString());
    }
}
