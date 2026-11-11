package com.fidecode.biteflow.application.auth;

import com.fidecode.biteflow.application.auth.dto.AccessibleStoreResponse;
import com.fidecode.biteflow.application.auth.dto.LoginRequest;
import com.fidecode.biteflow.application.auth.dto.LoginResponse;
import com.fidecode.biteflow.application.auth.port.PasswordEncoder;
import com.fidecode.biteflow.application.auth.port.TokenProvider;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.exception.InvalidCredentialsException;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final ListAccessibleStoresUseCase listAccessibleStoresUseCase;

    public LoginUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this(userRepository, passwordEncoder, tokenProvider, null);
    }

    @Inject
    public LoginUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenProvider tokenProvider,
            ListAccessibleStoresUseCase listAccessibleStoresUseCase) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.listAccessibleStoresUseCase = listAccessibleStoresUseCase;
    }

    public LoginResponse execute(LoginRequest request) {
        User user = userRepository.findByLogin(request.login()).orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(user);

        List<String> perms = user.getPermissions().stream().map(Permission::getValue).toList();

        List<AccessibleStoreResponse> accessibleStores = (listAccessibleStoresUseCase != null && user.getId() != null)
                ? listAccessibleStoresUseCase.execute(user.getId(), user.getStoreId())
                : List.of();

        return new LoginResponse(
                token,
                86400L,
                new LoginResponse.UserSummary(
                        user.getId() != null ? user.getId().toString() : null,
                        user.getName(),
                        user.getLogin(),
                        user.getRole().name(),
                        perms,
                        user.getStoreId().toString()
                ),
                accessibleStores
        );
    }
}
