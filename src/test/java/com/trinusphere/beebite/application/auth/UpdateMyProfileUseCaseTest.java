package com.trinusphere.beebite.application.auth;

import com.trinusphere.beebite.application.auth.dto.UpdateProfileRequest;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.exception.InvalidCredentialsException;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMyProfileUseCaseTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    private UpdateMyProfileUseCase useCase;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        useCase = new UpdateMyProfileUseCase(userRepository, passwordEncoder);
        userId = UUID.randomUUID();
        user = new User(
                UUID.randomUUID(),
                "Trinusphere Super Admin",
                "admin",
                "old_hash",
                Role.SUPER_ADMIN,
                Arrays.asList(Permission.values())
        );
        user.setId(userId);
    }

    @Test
    void shouldUpdateOwnNameWithoutChangingPassword() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse res = useCase.execute(userId, new UpdateProfileRequest("Sandro Junior", null, null));

        assertEquals("Sandro Junior", res.name());
        assertEquals("old_hash", user.getPasswordHash());
    }

    @Test
    void shouldUpdatePasswordWhenCurrentPasswordMatches() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaAtual", "old_hash")).thenReturn(true);
        when(passwordEncoder.hash("novaSenha123")).thenReturn("new_hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(userId, new UpdateProfileRequest("Admin", "senhaAtual", "novaSenha123"));

        assertEquals("new_hash", user.getPasswordHash());
    }

    @Test
    void shouldRejectPasswordChangeWhenCurrentPasswordIsWrong() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "old_hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(userId, new UpdateProfileRequest("Admin", "errada", "novaSenha123")));
        verify(userRepository, never()).save(any());
    }
}

