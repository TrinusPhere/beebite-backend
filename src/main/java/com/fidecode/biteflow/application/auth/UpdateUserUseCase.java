package com.fidecode.biteflow.application.auth;

import com.fidecode.biteflow.application.auth.dto.UpdateUserRequest;
import com.fidecode.biteflow.application.auth.dto.UserResponse;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.exception.UserNotFoundException;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import com.fidecode.biteflow.infrastructure.security.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Inject
    public UpdateUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public UserResponse execute(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordHasher.hash(request.password()));
        }
        if (request.role() != null) {
            user.setRole(Role.valueOf(request.role()));
        }
        if (request.permissions() != null) {
            List<Permission> perms = request.permissions().stream()
                .map(Permission::fromValue).toList();
            user.setPermissions(perms);
        }

        User saved = userRepository.save(user);
        return CreateUserUseCase.toResponse(saved);
    }
}
