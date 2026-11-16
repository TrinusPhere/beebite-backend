package com.trinusphere.beebite.application;

import com.trinusphere.beebite.application.auth.CreateUserUseCase;
import com.trinusphere.beebite.application.auth.dto.CreateUserRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.exception.UserAlreadyExistsException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import com.trinusphere.beebite.infrastructure.security.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
}
