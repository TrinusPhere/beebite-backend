package com.trinusphere.beebite.application;

import com.trinusphere.beebite.application.auth.ListUsersUseCase;
import com.trinusphere.beebite.application.auth.dto.UserResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
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

    @Test
    void shouldIncludeUsersWithSecondaryStoreAccessAndPopulateStoreIds() {
        UUID store1Id = storeId;
        UUID store2Id = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID secondaryUserId = UUID.randomUUID();
        User multiStoreManager = new User(store1Id, "Joao", "joao", "h", Role.GERENTE, List.of(Permission.PEDIDOS));
        multiStoreManager.setId(secondaryUserId);

        UserStoreAccessRepository accessRepo = mock(UserStoreAccessRepository.class);
        ListUsersUseCase multiUseCase = new ListUsersUseCase(userRepository, accessRepo);

        UserStoreAccess accessStore1 = new UserStoreAccess(secondaryUserId, store1Id, Role.GERENTE, List.of(Permission.PEDIDOS));
        UserStoreAccess accessStore2 = new UserStoreAccess(secondaryUserId, store2Id, Role.ADMIN, List.of(Permission.PEDIDOS, Permission.FINANCEIRO));

        when(userRepository.findAllByStoreId(store2Id)).thenReturn(List.of());
        when(accessRepo.findByStoreId(store2Id)).thenReturn(List.of(accessStore2));
        when(userRepository.findById(secondaryUserId)).thenReturn(Optional.of(multiStoreManager));
        when(accessRepo.findByUserId(secondaryUserId)).thenReturn(List.of(accessStore1, accessStore2));

        List<UserResponse> result = multiUseCase.execute(store2Id);

        assertEquals(1, result.size());
        assertEquals("Joao", result.get(0).name());
        assertEquals("ADMIN", result.get(0).role());
        assertEquals(2, result.get(0).storeIds().size());
    }
}
