package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.AccessibleStoreResponse;
import com.trinusphere.beebite.application.auth.dto.LoginResponse;
import com.trinusphere.beebite.application.auth.port.TokenProvider;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.InvalidCredentialsException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SwitchStoreUseCase {

    private final UserRepository userRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;
    private final TokenProvider tokenProvider;
    private final ListAccessibleStoresUseCase listAccessibleStoresUseCase;
    private final StoreRepository storeRepository;

    public SwitchStoreUseCase(
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository,
            TokenProvider tokenProvider,
            ListAccessibleStoresUseCase listAccessibleStoresUseCase) {
        this(userRepository, userStoreAccessRepository, tokenProvider, listAccessibleStoresUseCase, null);
    }

    @Inject
    public SwitchStoreUseCase(
            UserRepository userRepository,
            UserStoreAccessRepository userStoreAccessRepository,
            TokenProvider tokenProvider,
            ListAccessibleStoresUseCase listAccessibleStoresUseCase,
            StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
        this.tokenProvider = tokenProvider;
        this.listAccessibleStoresUseCase = listAccessibleStoresUseCase;
        this.storeRepository = storeRepository;
    }

    public LoginResponse execute(UUID userId, UUID targetStoreId) {
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        UserStoreAccess access;
        if (user.getRole() == Role.SUPER_ADMIN) {
            if (storeRepository != null) {
                storeRepository.findById(targetStoreId)
                        .orElseThrow(() -> new StoreNotFoundException(targetStoreId));
            }
            access = new UserStoreAccess(
                    userId,
                    targetStoreId,
                    Role.SUPER_ADMIN,
                    Arrays.asList(Permission.values())
            );
        } else {
            access = userStoreAccessRepository.findByUserAndStore(userId, targetStoreId)
                    .orElseGet(() -> {
                        if (user.getStoreId().equals(targetStoreId)
                                && userStoreAccessRepository.findByUserId(userId).isEmpty()) {
                            return new UserStoreAccess(userId, targetStoreId, user.getRole(), user.getPermissions());
                        }
                        throw new ForbiddenAccessException("Usuário não possui acesso à loja informada: " + targetStoreId);
                    });

            if (storeRepository != null) {
                Store targetStore = storeRepository.findById(targetStoreId)
                        .orElseThrow(() -> new StoreNotFoundException(targetStoreId));
                if (!targetStore.isActive()) {
                    throw new ForbiddenAccessException("Esta unidade está suspensa ou inativa no SaaS.");
                }
            }
        }

        String token = tokenProvider.generateTokenForStore(
                user,
                targetStoreId,
                access.getRole(),
                access.getPermissions()
        );

        List<String> perms = access.getPermissions().stream()
                .map(Permission::getValue)
                .toList();

        List<AccessibleStoreResponse> accessibleStores = listAccessibleStoresUseCase.execute(userId, targetStoreId);

        return new LoginResponse(
                token,
                86400L,
                new LoginResponse.UserSummary(
                        user.getId() != null ? user.getId().toString() : null,
                        user.getName(),
                        user.getLogin(),
                        access.getRole().name(),
                        perms,
                        targetStoreId.toString()
                ),
                accessibleStores
        );
    }
}
