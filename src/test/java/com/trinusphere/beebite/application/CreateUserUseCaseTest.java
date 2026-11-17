package com.trinusphere.beebite.application;

import com.trinusphere.beebite.application.auth.CreateUserUseCase;
import com.trinusphere.beebite.application.auth.dto.CreateUserRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserAlreadyExistsException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.infrastructure.security.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordHasher passwordHasher;
    @InjectMocks
    CreateUserUseCase createUserUseCase;

    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void shouldCreateUserAndReturnResponse() {
        CreateUserRequest request = new CreateUserRequest("Gerente", "gerente", "gerente123", "GERENTE",
                List.of("pedidos", "clientes"));
        when(userRepository.existsByLogin("gerente")).thenReturn(false);
        when(passwordHasher.hash("gerente123")).thenReturn("hashed_gerente123");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponse response = createUserUseCase.execute(STORE_ID, request);

        assertNotNull(response.id());
        assertEquals("Gerente", response.name());
        assertEquals("gerente", response.login());
        assertEquals("GERENTE", response.role());
    }

    @Test
    void shouldHashPasswordBeforeSaving() {
        CreateUserRequest request = new CreateUserRequest("Caixa", "caixa", "senhaPlain", "CAIXA", List.of());
        when(userRepository.existsByLogin("caixa")).thenReturn(false);
        when(passwordHasher.hash("senhaPlain")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        createUserUseCase.execute(STORE_ID, request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("$2a$hashed", captor.getValue().getPasswordHash());
        verify(passwordHasher).hash("senhaPlain");
    }

    @Test
    void shouldSetActiveTrueByDefault() {
        CreateUserRequest request = new CreateUserRequest("Novo", "novo", "pass", "CAIXA", List.of());
        when(userRepository.existsByLogin("novo")).thenReturn(false);
        when(passwordHasher.hash("pass")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        createUserUseCase.execute(STORE_ID, request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void shouldThrowWhenLoginAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("Admin2", "admin", "pass", "ADMIN", List.of());
        when(userRepository.existsByLogin("admin")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> createUserUseCase.execute(STORE_ID, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreatingSuperAdminRole() {
        CreateUserRequest request = new CreateUserRequest("Hacker", "hacker", "pass123", "SUPER_ADMIN", List.of());

        assertThrows(ForbiddenAccessException.class, () -> createUserUseCase.execute(STORE_ID, Role.ADMIN, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectGerenteCreatingAdminUser() {
        CreateUserRequest request = new CreateUserRequest("Novo Admin", "novoadmin", "pass123", "ADMIN", List.of());

        assertThrows(ForbiddenAccessException.class, () -> createUserUseCase.execute(STORE_ID, Role.GERENTE, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldCreateUserWithAccessToMultipleStoresOfSameTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID store2Id = UUID.fromString("00000000-0000-0000-0000-000000000002");
        Store store1 = Store.reconstitute(STORE_ID, "Chefs Loja 1", null, true, null, null, "chefs-1",
                "CLOUD", null, null, tenantId, null, true);
        Store store2 = Store.reconstitute(store2Id, "Chefs Loja 2", null, true, null, null, "chefs-2",
                "CLOUD", null, null, tenantId, null, true);

        UserStoreAccessRepository accessRepo = mock(UserStoreAccessRepository.class);
        StoreRepository storeRepo = mock(StoreRepository.class);
        CreateUserUseCase multiStoreUseCase = new CreateUserUseCase(userRepository, passwordHasher, accessRepo, storeRepo);

        CreateUserRequest request = new CreateUserRequest(
                "João Gerente", "joao.chefs", "senha123", "GERENTE",
                List.of("pedidos", "caixa"),
                List.of(STORE_ID.toString(), store2Id.toString())
        );

        when(storeRepo.findById(STORE_ID)).thenReturn(Optional.of(store1));
        when(storeRepo.findById(store2Id)).thenReturn(Optional.of(store2));
        when(userRepository.existsByLogin("joao.chefs")).thenReturn(false);
        when(passwordHasher.hash("senha123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponse response = multiStoreUseCase.execute(STORE_ID, Role.ADMIN, request);

        assertEquals(2, response.storeIds().size());
        assertTrue(response.storeIds().contains(STORE_ID.toString()));
        assertTrue(response.storeIds().contains(store2Id.toString()));
        verify(accessRepo, times(2)).save(any(UserStoreAccess.class));
    }

    @Test
    void shouldRejectCreatingUserInStoreFromDifferentTenant() {
        UUID tenant1 = UUID.randomUUID();
        UUID tenant2 = UUID.randomUUID();
        UUID foreignStoreId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        Store callerStore = Store.reconstitute(STORE_ID, "Chefs 1", null, true, null, null, "chefs-1",
                "CLOUD", null, null, tenant1, null, true);
        Store foreignStore = Store.reconstitute(foreignStoreId, "Concorrente", null, true, null, null, "conc",
                "CLOUD", null, null, tenant2, null, true);

        UserStoreAccessRepository accessRepo = mock(UserStoreAccessRepository.class);
        StoreRepository storeRepo = mock(StoreRepository.class);
        CreateUserUseCase multiStoreUseCase = new CreateUserUseCase(userRepository, passwordHasher, accessRepo, storeRepo);

        CreateUserRequest request = new CreateUserRequest(
                "Invasor", "invasor", "senha123", "CAIXA",
                List.of("pedidos"),
                List.of(foreignStoreId.toString())
        );

        when(storeRepo.findById(STORE_ID)).thenReturn(Optional.of(callerStore));
        when(storeRepo.findById(foreignStoreId)).thenReturn(Optional.of(foreignStore));

        assertThrows(ForbiddenAccessException.class,
                () -> multiStoreUseCase.execute(STORE_ID, Role.ADMIN, request));
        verify(userRepository, never()).save(any());
    }
}
