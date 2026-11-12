package com.fidecode.beebite.application;

import com.fidecode.beebite.application.auth.ListUsersUseCase;
import com.fidecode.beebite.application.auth.dto.UserResponse;
import com.fidecode.beebite.domain.auth.entity.User;
import com.fidecode.beebite.domain.auth.repository.UserRepository;
import com.fidecode.beebite.domain.auth.valueobject.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsersUseCaseTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    ListUsersUseCase listUsersUseCase;

    private final UUID storeId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void shouldReturnListOfUsers() {
        User u1 = new User(storeId, "Alice", "alice", "h", Role.GERENTE, List.of());
        User u2 = new User(storeId, "Bob", "bob", "h", Role.CAIXA, List.of());
        when(userRepository.findAllByStoreId(storeId)).thenReturn(List.of(u1, u2));

        List<UserResponse> result = listUsersUseCase.execute(storeId);

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).name());
        assertEquals("Bob", result.get(1).name());
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        when(userRepository.findAllByStoreId(storeId)).thenReturn(List.of());

        List<UserResponse> result = listUsersUseCase.execute(storeId);

        assertTrue(result.isEmpty());
    }
}
