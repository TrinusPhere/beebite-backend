package com.fidecode.biteflow.domain;

import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void shouldCreateUserWithRequiredFields() {
        User user = new User(STORE_ID, "João Silva", "joao", "hash123", Role.CAIXA, List.of());
        assertEquals("João Silva", user.getName());
        assertEquals("joao", user.getLogin());
        assertEquals(Role.CAIXA, user.getRole());
    }

    @Test
    void shouldBeActiveByDefault() {
        User user = new User(STORE_ID, "João", "joao", "hash", Role.CAIXA, List.of());
        assertTrue(user.isActive());
    }

    @Test
    void shouldStorePermissions() {
        List<Permission> permissions = List.of(Permission.PEDIDOS, Permission.CLIENTES);
        User user = new User(STORE_ID, "Gerente", "gerente", "hash", Role.GERENTE, permissions);
        assertTrue(user.getPermissions().contains(Permission.PEDIDOS));
        assertTrue(user.getPermissions().contains(Permission.CLIENTES));
        assertEquals(2, user.getPermissions().size());
    }

    @Test
    void shouldHaveAllPermissionsForAdmin() {
        User admin = User.createAdmin(STORE_ID, "Admin", "admin", "hash");
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals(Permission.values().length, admin.getPermissions().size());
    }

    @Test
    void shouldRejectNullStoreId() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(null, "Nome", "login", "hash", Role.CAIXA, List.of()));
    }

    @Test
    void shouldRejectBlankLogin() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(STORE_ID, "Nome", "  ", "hash", Role.CAIXA, List.of()));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(STORE_ID, "", "login", "hash", Role.CAIXA, List.of()));
    }

    @Test
    void shouldAllowDeactivation() {
        User user = new User(STORE_ID, "João", "joao", "hash", Role.CAIXA, List.of());
        assertTrue(user.isActive());
        user.deactivate();
        assertFalse(user.isActive());
    }
}
