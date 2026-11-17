package com.trinusphere.beebite.application;

import com.trinusphere.beebite.application.auth.UpdateUserUseCase;
import com.trinusphere.beebite.application.auth.dto.UpdateUserRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordHasher;
    @InjectMocks
    UpdateUserUseCase updateUserUseCase;

    private User existingUser;
    private final UUID userId = UUID.randomUUID();
    private final UUID storeId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        existingUser = new User(storeId, "Old Name", "oldLogin", "oldHash", Role.CAIXA, List.of());
        existingUser.setId(userId);
    }

    @Test
    void shouldUpdateUserFields() {
        UpdateUserRequest request = new UpdateUserRequest("New Name", null, "GERENTE", List.of("pedidos"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = updateUserUseCase.execute(storeId, userId, request);

        assertEquals("New Name", response.name());
        assertEquals("GERENTE", response.role());
        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void shouldHashNewPasswordWhenProvided() {
        UpdateUserRequest request = new UpdateUserRequest("Old Name", "newPass123", "CAIXA", List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordHasher.hash("newPass123")).thenReturn("newHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        updateUserUseCase.execute(storeId, userId, request);

        verify(passwordHasher).hash("newPass123");
    }

    @Test
    void shouldNotHashWhenPasswordNotProvided() {
        UpdateUserRequest request = new UpdateUserRequest("Name", null, "CAIXA", List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        updateUserUseCase.execute(storeId, userId, request);

        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> updateUserUseCase.execute(storeId, userId, new UpdateUserRequest("n", null, "CAIXA", List.of())));
    }

    @Test
    void shouldRejectPromotingUserToSuperAdmin() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertThrows(ForbiddenAccessException.class, () -> updateUserUseCase.execute(
                storeId, Role.ADMIN, userId, new UpdateUserRequest("Name", null, "SUPER_ADMIN", List.of())));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectEditingSuperAdminUser() {
        User superAdmin = new User(storeId, "Super", "super", "hash", Role.SUPER_ADMIN, List.of());
        superAdmin.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(superAdmin));

        assertThrows(ForbiddenAccessException.class, () -> updateUserUseCase.execute(
                storeId, Role.ADMIN, userId, new UpdateUserRequest("Hack", null, "ADMIN", List.of())));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectGerentePromotingToAdmin() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertThrows(ForbiddenAccessException.class, () -> updateUserUseCase.execute(
                storeId, Role.GERENTE, userId, new UpdateUserRequest("Name", null, "ADMIN", List.of())));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldSyncStoreAccessesWhenUpdatingMultiStoreUser() {
        UUID tenantId = UUID.randomUUID();
        UUID store2Id = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID store3Id = UUID.fromString("00000000-0000-0000-0000-000000000003");
        Store store1 = Store.reconstitute(storeId, "Loja 1", null, true, null, null, "l1",
                "CLOUD", null, null, tenantId, null, true);
        Store store3 = Store.reconstitute(store3Id, "Loja 3", null, true, null, null, "l3",
                "CLOUD", null, null, tenantId, null, true);

        UserStoreAccessRepository accessRepo = mock(UserStoreAccessRepository.class);
        StoreRepository storeRepo = mock(StoreRepository.class);
        UpdateUserUseCase multiUseCase = new UpdateUserUseCase(userRepository, passwordHasher, accessRepo, storeRepo);

        UserStoreAccess access1 = new UserStoreAccess(userId, storeId, Role.CAIXA, List.of());
        UserStoreAccess access2 = new UserStoreAccess(userId, store2Id, Role.CAIXA, List.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(storeRepo.findById(storeId)).thenReturn(Optional.of(store1));
        when(storeRepo.findById(store3Id)).thenReturn(Optional.of(store3));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accessRepo.findByUserId(userId)).thenReturn(List.of(access1, access2));
        when(accessRepo.findByUserAndStore(userId, storeId)).thenReturn(Optional.of(access1));
        when(accessRepo.findByUserAndStore(userId, store3Id)).thenReturn(Optional.empty());
        when(accessRepo.save(any(UserStoreAccess.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest(
                "Gerente Multi", null, "GERENTE", List.of("pedidos", "dashboard"),
                List.of(storeId.toString(), store3Id.toString()));

        UserResponse res = multiUseCase.execute(storeId, Role.ADMIN, userId, request);

        assertEquals("GERENTE", res.role());
        assertEquals(2, res.storeIds().size());
        verify(accessRepo).deleteByUserAndStore(userId, store2Id);
        verify(accessRepo, times(2)).save(any(UserStoreAccess.class));
        assertEquals(Role.GERENTE, access1.getRole());
        assertTrue(access1.getPermissions().contains(Permission.PEDIDOS));
    }
}
