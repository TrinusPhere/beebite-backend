package com.fidecode.biteflow.domain;

import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermissionTest {

    @Test
    void shouldHaveAllRequiredPermissions() {
        Set<String> expected = Set.of(
            "dashboard", "pedidos", "clientes", "ingredientes",
            "produtos", "entregadores", "acerto_entregadores",
            "usuarios", "integracoes", "financeiro"
        );
        for (String perm : expected) {
            assertDoesNotThrow(() -> Permission.fromValue(perm),
                "Permission ausente: " + perm);
        }
    }

    @Test
    void shouldReturnValueString() {
        assertEquals("dashboard", Permission.DASHBOARD.getValue());
        assertEquals("pedidos", Permission.PEDIDOS.getValue());
        assertEquals("usuarios", Permission.USUARIOS.getValue());
    }

    @Test
    void shouldThrowForUnknownPermission() {
        assertThrows(IllegalArgumentException.class,
            () -> Permission.fromValue("permissao_invalida"));
    }
}
