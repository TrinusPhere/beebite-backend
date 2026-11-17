package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.AccessibleStoreResponse;
import com.trinusphere.beebite.application.auth.dto.LoginRequest;
import com.trinusphere.beebite.application.auth.dto.LoginResponse;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.auth.port.TokenProvider;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.exception.InvalidCredentialsException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final ListAccessibleStoresUseCase listAccessibleStoresUseCase;
    private final StoreRepository storeRepository;

    public LoginUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this(userRepository, passwordEncoder, tokenProvider, null, null);
    }

    public LoginUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenProvider tokenProvider,
            ListAccessibleStoresUseCase listAccessibleStoresUseCase) {
        this(userRepository, passwordEncoder, tokenProvider, listAccessibleStoresUseCase, null);
    }

    @Inject
    public LoginUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenProvider tokenProvider,
            ListAccessibleStoresUseCase listAccessibleStoresUseCase,
            StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.listAccessibleStoresUseCase = listAccessibleStoresUseCase;
        this.storeRepository = storeRepository;
    }

    public LoginResponse execute(LoginRequest request) {
        String cleanLogin = request.login() != null ? request.login().trim() : "";
        User user = userRepository.findByLogin(cleanLogin).orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        UUID effectiveStoreId = user.getStoreId();
        List<AccessibleStoreResponse> accessibleStores = (listAccessibleStoresUseCase != null && user.getId() != null)
                ? listAccessibleStoresUseCase.execute(user.getId(), effectiveStoreId)
                : List.of();

        if (user.getRole() != Role.SUPER_ADMIN) {
            if (listAccessibleStoresUseCase != null && user.getId() != null) {
                String currentStoreIdStr = effectiveStoreId.toString();
                boolean homeStoreAccessible = accessibleStores.stream()
                        .anyMatch(s -> s.storeId().equals(currentStoreIdStr) && s.storeActive());

                if (!homeStoreAccessible) {
                    Optional<AccessibleStoreResponse> activeAlternative = accessibleStores.stream()
                            .filter(AccessibleStoreResponse::storeActive)
                            .findFirst();
                    if (activeAlternative.isEmpty()) {
                        throw new ForbiddenAccessException(
                                "Unidade suspensa ou inativa no SaaS. Entre em contato com o suporte Trinusphere.");
                    }
                    effectiveStoreId = UUID.fromString(activeAlternative.get().storeId());
                    accessibleStores = listAccessibleStoresUseCase.execute(user.getId(), effectiveStoreId);
                }
            } else if (storeRepository != null) {
                Optional<Store> primaryStoreOpt = storeRepository.findById(user.getStoreId());
                if (primaryStoreOpt.isPresent() && !primaryStoreOpt.get().isActive()) {
                    throw new ForbiddenAccessException(
                            "Unidade suspensa ou inativa no SaaS. Entre em contato com o suporte Trinusphere.");
                }
            }
        }

        String finalStoreIdStr = effectiveStoreId.toString();
        AccessibleStoreResponse activeAccess = accessibleStores.stream()
                .filter(s -> s.storeId().equals(finalStoreIdStr))
                .findFirst()
                .orElse(null);

        Role effectiveRole = (activeAccess != null && activeAccess.role() != null)
                ? Role.valueOf(activeAccess.role())
                : user.getRole();
        List<Permission> effectivePermissions = (activeAccess != null && activeAccess.permissions() != null)
                ? activeAccess.permissions().stream().map(Permission::fromValue).toList()
                : user.getPermissions();

        boolean matchesPrimaryDefaults = effectiveStoreId.equals(user.getStoreId())
                && effectiveRole == user.getRole()
                && effectivePermissions.equals(user.getPermissions());

        String token = matchesPrimaryDefaults
                ? tokenProvider.generateToken(user)
                : tokenProvider.generateTokenForStore(user, effectiveStoreId, effectiveRole, effectivePermissions);

        List<String> perms = effectivePermissions.stream().map(Permission::getValue).toList();

        return new LoginResponse(
                token,
                86400L,
                new LoginResponse.UserSummary(
                        user.getId() != null ? user.getId().toString() : null,
                        user.getName(),
                        user.getLogin(),
                        effectiveRole.name(),
                        perms,
                        finalStoreIdStr
                ),
                accessibleStores
        );
    }
}
