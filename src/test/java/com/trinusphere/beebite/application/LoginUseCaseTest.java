package com.trinusphere.beebite.application;

import com.trinusphere.beebite.application.auth.ListAccessibleStoresUseCase;
import com.trinusphere.beebite.application.auth.LoginUseCase;
import com.trinusphere.beebite.application.auth.dto.AccessibleStoreResponse;
import com.trinusphere.beebite.application.auth.dto.LoginRequest;
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
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.infrastructure.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordHasher passwordHasher;
    @Mock
    TokenProvider tokenProvider;
    @InjectMocks
    LoginUseCase loginUseCase;

    private User adminUser;
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        adminUser = User.createAdmin(STORE_ID, "Admin", "admin", "hashed_password");
        adminUser.setId(UUID.randomUUID());
    }

    @Test
    void shouldReturnLoginResponseWithTokenOnValidCredentials() {
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(adminUser));
        when(passwordHasher.matches("admin123", "hashed_password")).thenReturn(true);
        when(tokenProvider.generateToken(adminUser)).thenReturn("jwt.token.here");

        LoginResponse response = loginUseCase.execute(new LoginRequest("admin", "admin123"));

        assertNotNull(response.token());
        assertEquals("jwt.token.here", response.token());
        assertEquals("admin", response.user().login());
        assertEquals("ADMIN", response.user().role());
        assertFalse(response.user().permissions().isEmpty());
    }

    @Test
    void shouldThrowWhenLoginNotFound() {
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> loginUseCase.execute(new LoginRequest("unknown", "pass")));
    }

    @Test
    void shouldThrowWhenPasswordIncorrect() {
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(adminUser));
        when(passwordHasher.matches("wrong", "hashed_password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(new LoginRequest("admin", "wrong")));
    }

    @Test
    void shouldIncludePermissionsInResponse() {
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(adminUser));
        when(passwordHasher.matches("admin123", "hashed_password")).thenReturn(true);
        when(tokenProvider.generateToken(adminUser)).thenReturn("token");

        LoginResponse response = loginUseCase.execute(new LoginRequest("admin", "admin123"));

        assertTrue(response.user().permissions().contains("dashboard"));
        assertTrue(response.user().permissions().contains("pedidos"));
        assertEquals(Permission.values().length, response.user().permissions().size());
    }

    @Test
    void shouldThrowWhenUserIsInactive() {
        adminUser.deactivate();
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(adminUser));
        // Não configura passwordHasher pois a exceção é lançada antes de chegar lá

        assertThrows(InvalidCredentialsException.class,
                () -> loginUseCase.execute(new LoginRequest("admin", "admin123")));
    }

    @Test
    void shouldThrowForbiddenWhenStoreIsSuspendedOrInactiveForClientAdmin() {
        StoreRepository storeRepoMock = mock(StoreRepository.class);
        LoginUseCase useCaseWithStoreCheck = new LoginUseCase(
                userRepository, passwordHasher, tokenProvider, null, storeRepoMock);

        Store suspendedStore = Store.reconstitute(
                STORE_ID, "Loja Suspensa", null, false, null, null, null,
                "CLOUD", null, null, UUID.randomUUID(), null, false);

        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(adminUser));
        when(passwordHasher.matches("admin123", "hashed_password")).thenReturn(true);
        when(storeRepoMock.findById(STORE_ID)).thenReturn(Optional.of(suspendedStore));

        assertThrows(ForbiddenAccessException.class,
                () -> useCaseWithStoreCheck.execute(new LoginRequest("admin", "admin123")));
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void shouldLoginIntoActiveAccessibleStoreWhenOriginalHomeStoreAccessWasRevoked() {
        UUID store2Id = UUID.fromString("00000000-0000-0000-0000-000000000002");
        Store store1 = Store.reconstitute(STORE_ID, "Loja 1", null, true, null, null, "l1");
        Store store2 = Store.reconstitute(store2Id, "Loja 2", null, true, null, null, "l2");

        User multiUser = new User(STORE_ID, "Joao", "joao", "hashed_password", Role.CAIXA, List.of(Permission.CAIXA));
        UUID joaoId = UUID.randomUUID();
        multiUser.setId(joaoId);

        UserStoreAccessRepository accessRepo = mock(UserStoreAccessRepository.class);
        StoreRepository storeRepo = mock(StoreRepository.class);
        ListAccessibleStoresUseCase listStoresUseCase = new ListAccessibleStoresUseCase(userRepository, accessRepo, storeRepo);
        LoginUseCase multiLoginUseCase = new LoginUseCase(
                userRepository, passwordHasher, tokenProvider, listStoresUseCase, storeRepo);

        UserStoreAccess accessStore2 = new UserStoreAccess(joaoId, store2Id, Role.GERENTE, List.of(Permission.PEDIDOS));

        when(userRepository.findByLogin("joao")).thenReturn(Optional.of(multiUser));
        when(passwordHasher.matches("pass123", "hashed_password")).thenReturn(true);
        when(userRepository.findById(joaoId)).thenReturn(Optional.of(multiUser));
        when(accessRepo.findByUserId(joaoId)).thenReturn(List.of(accessStore2));
        when(storeRepo.findById(store2Id)).thenReturn(Optional.of(store2));
        when(tokenProvider.generateTokenForStore(multiUser, store2Id, Role.GERENTE, List.of(Permission.PEDIDOS)))
                .thenReturn("jwt.store2");

        LoginResponse response = multiLoginUseCase.execute(new LoginRequest("joao", "pass123"));

        assertEquals("jwt.store2", response.token());
        assertEquals(store2Id.toString(), response.user().storeId());
        assertEquals("GERENTE", response.user().role());
        assertEquals(List.of("pedidos"), response.user().permissions());
    }
}
