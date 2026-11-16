package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.UpdateUserRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Inject
    public UpdateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse execute(UUID storeId, UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        StoreGuard.enforce(user, storeId);

        if (request.name() != null && !request.name().isBlank()) {
            user.rename(request.name());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.changePasswordHash(passwordEncoder.hash(request.password()));
        }
        if (request.role() != null) {
            user.changeRole(Role.valueOf(request.role()));
        }
        if (request.permissions() != null) {
            List<Permission> perms = request.permissions().stream().map(Permission::fromValue).toList();
            user.replacePermissions(perms);
        }

        User saved = userRepository.save(user);
        return CreateUserUseCase.toResponse(saved);
    }
}
