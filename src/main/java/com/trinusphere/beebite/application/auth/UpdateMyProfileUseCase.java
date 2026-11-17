package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.UpdateProfileRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.exception.InvalidCredentialsException;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateMyProfileUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Inject
    public UpdateMyProfileUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse execute(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.name() != null && !request.name().isBlank()) {
            user.rename(request.name().trim());
        }

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null
                    || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new InvalidCredentialsException();
            }
            user.changePasswordHash(passwordEncoder.hash(request.newPassword()));
        }

        User saved = userRepository.save(user);
        return CreateUserUseCase.toResponse(saved);
    }
}

