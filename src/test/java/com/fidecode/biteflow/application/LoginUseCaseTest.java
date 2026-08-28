package com.fidecode.biteflow.application;

import com.fidecode.biteflow.application.auth.LoginUseCase;
import com.fidecode.biteflow.application.auth.dto.LoginRequest;
import com.fidecode.biteflow.application.auth.dto.LoginResponse;
import com.fidecode.biteflow.application.auth.port.TokenProvider;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.exception.InvalidCredentialsException;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import com.fidecode.biteflow.infrastructure.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
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
}
