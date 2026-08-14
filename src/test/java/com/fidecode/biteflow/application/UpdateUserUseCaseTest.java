package com.fidecode.biteflow.application;

import com.fidecode.biteflow.application.auth.UpdateUserUseCase;
import com.fidecode.biteflow.application.auth.dto.UpdateUserRequest;
import com.fidecode.biteflow.application.auth.dto.UserResponse;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.exception.UserNotFoundException;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import com.fidecode.biteflow.infrastructure.security.PasswordHasher;
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

    @Mock UserRepository userRepository;
    @Mock PasswordHasher passwordHasher;
    @InjectMocks UpdateUserUseCase updateUserUseCase;

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
        UpdateUserRequest request = new UpdateUserRequest(
            "New Name", null, "GERENTE", List.of("pedidos")
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = updateUserUseCase.execute(userId, request);

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

        updateUserUseCase.execute(userId, request);

        verify(passwordHasher).hash("newPass123");
    }

    @Test
    void shouldNotHashWhenPasswordNotProvided() {
        UpdateUserRequest request = new UpdateUserRequest("Name", null, "CAIXA", List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        updateUserUseCase.execute(userId, request);

        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
            () -> updateUserUseCase.execute(userId, new UpdateUserRequest("n", null, "CAIXA", List.of())));
    }
}
