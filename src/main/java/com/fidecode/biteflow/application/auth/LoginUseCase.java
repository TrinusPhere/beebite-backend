package com.fidecode.biteflow.application.auth;

import com.fidecode.biteflow.application.auth.dto.LoginRequest;
import com.fidecode.biteflow.application.auth.dto.LoginResponse;
import com.fidecode.biteflow.application.auth.port.TokenProvider;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.exception.InvalidCredentialsException;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.infrastructure.security.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    @Inject
    public LoginUseCase(UserRepository userRepository,
                        PasswordHasher passwordHasher,
                        TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse execute(LoginRequest request) {
        User user = userRepository.findByLogin(request.login())
            .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(user);

        List<String> perms = user.getPermissions().stream()
            .map(p -> p.getValue())
            .toList();

        return new LoginResponse(
            token,
            86400L,
            new LoginResponse.UserSummary(
                user.getId() != null ? user.getId().toString() : null,
                user.getName(),
                user.getLogin(),
                user.getRole().name(),
                perms
            )
        );
    }
}
