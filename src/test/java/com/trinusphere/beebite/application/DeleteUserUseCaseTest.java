package com.trinusphere.beebite.application;

import com.trinusphere.beebite.application.auth.DeleteUserUseCase;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.exception.UserNotFoundException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    DeleteUserUseCase deleteUserUseCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID storeId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void shouldDeleteExistingUser() {
        User user = new User(storeId, "Nome", "login", "hash", Role.CAIXA, List.of());
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        deleteUserUseCase.execute(storeId, userId);

        verify(userRepository).delete(userId);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> deleteUserUseCase.execute(storeId, userId));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void shouldRejectDeletingSuperAdminUser() {
        User superAdmin = new User(storeId, "Super", "admin", "hash", Role.SUPER_ADMIN, List.of());
        superAdmin.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(superAdmin));

        assertThrows(ForbiddenAccessException.class, () -> deleteUserUseCase.execute(storeId, Role.ADMIN, userId));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void shouldRejectGerenteDeletingAdminUser() {
        User storeAdmin = new User(storeId, "Dono", "dono", "hash", Role.ADMIN, List.of());
        storeAdmin.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(storeAdmin));

        assertThrows(ForbiddenAccessException.class, () -> deleteUserUseCase.execute(storeId, Role.GERENTE, userId));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void shouldAllowDeletingUserWithSecondaryAccessInCallerStore() {
        UUID homeStoreId = UUID.randomUUID();
        User multiUser = new User(homeStoreId, "Joao", "joao", "hash", Role.CAIXA, List.of());
        multiUser.setId(userId);

        UserStoreAccessRepository accessRepo = mock(UserStoreAccessRepository.class);
        DeleteUserUseCase multiDelete = new DeleteUserUseCase(userRepository, accessRepo);

        when(userRepository.findById(userId)).thenReturn(Optional.of(multiUser));
        when(accessRepo.findByUserAndStore(userId, storeId))
                .thenReturn(Optional.of(new UserStoreAccess(userId, storeId, Role.CAIXA, List.of())));

        multiDelete.execute(storeId, Role.ADMIN, userId);

        verify(userRepository).delete(userId);
    }
}
