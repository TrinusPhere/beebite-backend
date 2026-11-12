package com.fidecode.beebite.domain.auth;

import com.fidecode.beebite.domain.auth.entity.UserStoreAccess;
import com.fidecode.beebite.domain.auth.valueobject.Permission;
import com.fidecode.beebite.domain.auth.valueobject.Role;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserStoreAccessTest {

    @Test
    void shouldCreateValidUserStoreAccess() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        UserStoreAccess access = new UserStoreAccess(
                userId,
                storeId,
                Role.ADMIN,
                List.of(Permission.DASHBOARD, Permission.PEDIDOS)
        );

        assertEquals(userId, access.getUserId());
        assertEquals(storeId, access.getStoreId());
        assertEquals(Role.ADMIN, access.getRole());
        assertEquals(2, access.getPermissions().size());
        assertNotNull(access.getCreatedAt());
    }

    @Test
    void shouldReconstituteUserStoreAccess() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(60);

        UserStoreAccess access = UserStoreAccess.reconstitute(
                id,
                userId,
                storeId,
                Role.GERENTE,
                List.of(Permission.FINANCEIRO),
                createdAt
        );

        assertEquals(id, access.getId());
        assertEquals(userId, access.getUserId());
        assertEquals(storeId, access.getStoreId());
        assertEquals(Role.GERENTE, access.getRole());
        assertEquals(List.of(Permission.FINANCEIRO), access.getPermissions());
        assertEquals(createdAt, access.getCreatedAt());
    }

    @Test
    void shouldUpdateRoleAndPermissions() {
        UserStoreAccess access = new UserStoreAccess(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Role.CAIXA,
                List.of(Permission.PEDIDOS)
        );

        access.updateAccess(Role.GERENTE, List.of(Permission.PEDIDOS, Permission.FINANCEIRO));

        assertEquals(Role.GERENTE, access.getRole());
        assertEquals(2, access.getPermissions().size());
    }

    @Test
    void shouldRejectNullRequiredFields() {
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> new UserStoreAccess(null, id, Role.ADMIN, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new UserStoreAccess(id, null, Role.ADMIN, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new UserStoreAccess(id, id, null, List.of()));
    }
}
