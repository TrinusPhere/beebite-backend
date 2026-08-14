package com.fidecode.biteflow.application;

import com.fidecode.biteflow.application.auth.DeleteUserUseCase;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.exception.UserNotFoundException;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
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

    @Mock UserRepository userRepository;
    @InjectMocks DeleteUserUseCase deleteUserUseCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID storeId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void shouldDeleteExistingUser() {
        User user = new User(storeId, "Nome", "login", "hash", Role.CAIXA, List.of());
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        deleteUserUseCase.execute(userId);

        verify(userRepository).delete(userId);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> deleteUserUseCase.execute(userId));
        verify(userRepository, never()).delete(any());
    }
}
